package ntou.cs.project.Service;

import org.springframework.stereotype.Service;
import ntou.cs.project.Common.*;
import ntou.cs.project.Deal.*;
import ntou.cs.project.repository.*;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

@Service
public class MyService {

	@Autowired
	private AccountRepository repository;

	@Autowired
	private RecurringAccountRepository recurrRepository;

	@Autowired
	private GridFsTemplate gridFsTemplate;

	public String saveAttach(MultipartFile attach) {
		try {
			ObjectId fileId = gridFsTemplate.store(
					attach.getInputStream(), attach.getOriginalFilename(), attach.getContentType());
			return fileId.toString();
		} catch (IOException e) {
			throw new RuntimeException("Error saving file to GridFS", e);
		}
	}

	public Account createAccount(AccountRequest request, String attach, String userID) {
		Account accounts = new Account();
		accounts.setRemark(request.getRemark());
		accounts.setAccType(request.getAccType());
		accounts.setCategory(request.getCategory());
		accounts.setAttach(attach);
		accounts.setPrice(request.getPrice());
		accounts.setUserID(userID);

		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		accounts.setTime(LocalDateTime.parse(request.getTime(), formatter));

		if (attach != null) {
			accounts.setAttachURL("/getAttach/" + attach);
		}

		repository.insert(accounts);
		if (request.getisRecurring() == true) {
			RecurringAccount ra = new RecurringAccount(accounts);
			ra.setActive(true);
			ra.setRecurrenceType(request.getrecurrenceType());
			DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
			ra.setLastGenerate(LocalDate.parse(request.getTime().split("T")[0], dateFormatter));
			ra.setRecurrenceEndDate(LocalDate.parse(request.getRecurrenceEndDate(), dateFormatter));
			recurrRepository.insert(ra);
		}

		return accounts;
	}

	public void deleteAccount(String id) {
		repository.deleteById(id);
		recurrRepository.deleteById(id);
	}

	public ArrayList<?> getAccounts(QueryParameter param, String userID) {
		String category = param.getCategory(); // 類別
		String startTime = param.getStartTime(); // 起始時間
		String endTime = param.getEndTime(); // 結束時間
		Sort sort = Sort.by(Sort.Direction.DESC, "time"); // 時間按照倒敘排序

		if (category != null && startTime != null && endTime != null) {
			return repository.getAccountsByCategoryAndTimeBetween(userID, category, startTime, endTime);
		} else if (startTime != null && endTime != null) {
			return repository.getAccountsByTimeAfterAndTimeBetween(userID, startTime, endTime);
		}

		ArrayList<Account> accounts = repository.getAllAccountsByUserId(userID, sort);
		ArrayList<RecurringAccount> recurringAccounts = recurrRepository.findByUserId(userID);
		Map<String, RecurringAccount> recurringMap = recurringAccounts.stream()
				.collect(Collectors.toMap(RecurringAccount::getID, ra -> ra));

		List<AccountWithResponse> response = accounts.stream()
				.map(account -> new AccountWithResponse(
						account,
						recurringMap.get(account.getID())))
				.collect(Collectors.toList());
		return new ArrayList<>(response);
		// return new ArrayList<>(repository.getAllAccountsByUserId(userID, sort));
	}

	public Account getAccount(String id) { // 回傳單筆帳目
		return repository.getAccountByID(id);
	}

	public Account updateAccount(String id, AccountRequest request, MultipartFile attach) throws IOException {
		Account oldAccount = getAccount(id);

		oldAccount.setCategory(request.getCategory());
		oldAccount.setAccType(request.getAccType());
		oldAccount.setRemark(request.getRemark());
		oldAccount.setPrice(request.getPrice());
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		LocalDateTime time = LocalDateTime.parse(request.getTime(), formatter);
		oldAccount.setTime(LocalDateTime.parse(time.toString()));

		if (attach != null && !attach.isEmpty()) {
			String girdAttach = saveAttach(attach);
			oldAccount.setAttach(girdAttach);
			oldAccount.setAttachURL("/getAttach/" + girdAttach);
		}

		// 資料庫儲存變更
		return repository.save(oldAccount);
	}

	@Scheduled(cron = "0 0 0 * * ?")
	public void handleRecurringAccounts() {
		ArrayList<RecurringAccount> recurringAccounts = recurrRepository.findByIsRecurring(true);

		for (RecurringAccount account : recurringAccounts) {
			if (shouldCreateEntry(account)) {
				Account newAccount = new Account();
				newAccount.setUserID(account.getUserID());
				newAccount.setCategory(account.getCategory());
				newAccount.setRemark(account.getRemark());
				newAccount.setAccType(account.getAccType());
				newAccount.setAttach(account.getAttach());
				newAccount.setAttachURL(account.getAttachURL());
				newAccount.setPrice(account.getPrice());
				newAccount.setTime(LocalDateTime.now());

				repository.save(newAccount);
			}
		}
	}

	private boolean shouldCreateEntry(RecurringAccount account) {
		LocalDate startDate = account.getLastGenerate();
		LocalDate endDate = account.getRecurrenceEndDate();
		LocalDate today = LocalDate.now();

		if ((today.isAfter(endDate))) {
			return false;
		}

		switch (account.getRecurrenceType()) {
			case "DAILY":
				return true;
			case "WEEKLY":
				return startDate.getDayOfWeek() == today.getDayOfWeek();
			case "MONTHLY":
				return startDate.getDayOfMonth() == today.getDayOfMonth();
			case "YEARLY":
				return startDate.getMonth() == today.getMonth() && startDate.getDayOfMonth() == today.getDayOfMonth();
			default:
				return false;
		}
	}

}