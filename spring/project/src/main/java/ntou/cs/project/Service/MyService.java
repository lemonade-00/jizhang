package ntou.cs.project.Service;

import org.springframework.stereotype.Service;
import ntou.cs.project.Common.Account;
import ntou.cs.project.Deal.*;
import ntou.cs.project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.util.UUID;
import java.io.IOException;

@Service
public class MyService {

	@Autowired
	private AccountRepository repository;

	public Account createAccount(AccountRequest request, String filePath) {
		Account accounts = new Account();
		accounts.setRemark(request.getRemark());
		accounts.setCategory(request.getCategory());
		accounts.setAttach(filePath);
		accounts.setPrice(request.getPrice());

		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		LocalDateTime time = LocalDateTime.parse(request.getTime(), formatter);
		accounts.setTime(time);

		// 加入資料庫
		repository.insert(accounts);

		return accounts;
	}

	public String saveAttach(MultipartFile attach, String uploadDir) throws IOException { // 儲存附件
		String uniqueFileName = UUID.randomUUID().toString() + "_" + attach.getOriginalFilename();
		String filePath = uploadDir + "/" + uniqueFileName;
		Path destinationPath = Paths.get(filePath);
		Files.copy(attach.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
		return filePath;
	}

	public void deleteAccount(String id) {
		repository.deleteById(id);
	}

	public ArrayList<Account> getAccounts(QueryParameter param) {
		String category = param.getCategory(); // 類別
		String startTime = param.getStartTime(); // 起始時間
		String endTime = param.getEndTime(); // 結束時間
		Sort sort = Sort.by(Sort.Direction.DESC, "time"); // 時間按照倒敘排序

		if (category != null && startTime != null && endTime != null) {
			return repository.getAccountsByCategoryAndTimeBetween(category, startTime, endTime);
		} else if (startTime != null && endTime != null) {
			return repository.getAccountsByTimeAfterAndTimeBetween(startTime, endTime);
		}
		// 沒有篩選條件，回傳該用戶全部帳目
		return new ArrayList<>(repository.findAll(sort));
	}

	public Account getAccount(String id) { // 回傳單筆帳目
		return repository.getAccountByID(id);
	}

	public Account updateAccount(String id, AccountRequest request, MultipartFile attach) throws IOException {
		Account oldAccount = getAccount(id);

		if (request.getCategory() != null) {
			oldAccount.setCategory(request.getCategory());
		}
		if (request.getRemark() != null) {
			oldAccount.setRemark(request.getRemark());
		}
		if (request.getTime() != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			LocalDateTime time = LocalDateTime.parse(request.getTime(), formatter);
			oldAccount.setTime(LocalDateTime.parse(time.toString()));
		}
		if (request.getPrice() != 0) {
			oldAccount.setPrice(request.getPrice());
		}
		if (attach != null && !attach.isEmpty()) {
			// 刪除舊附件
			String oldFilePath = oldAccount.getAttach();
			if (oldFilePath != null) {
				Path oldFile = Paths.get(oldFilePath);
				Files.deleteIfExists(oldFile);
			}
			// 儲存新附件
			String newFilePath = saveAttach(attach, "upload");
			oldAccount.setAttach(newFilePath);
		}

		// 資料庫儲存變更
		return repository.save(oldAccount);
	}
}