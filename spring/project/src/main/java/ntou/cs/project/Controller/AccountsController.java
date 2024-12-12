package ntou.cs.project.Controller;

import ntou.cs.project.Service.MyService;
import ntou.cs.project.Common.*;
import ntou.cs.project.Deal.*;
import ntou.cs.project.repository.*;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.model.GridFSFile;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping(value = "/Account")
public class AccountsController {

	@Autowired
	private MyService myService;

	@Autowired
	private AccountRepository repository;

	@Autowired
	private GridFsTemplate gridFsTemplate;

	@PostMapping(value = "/addAccount", consumes = "multipart/form-data") // 新增帳目
	public ResponseEntity<Account> createAccount(
			@RequestParam("data") String data,
			@RequestParam(value = "attach", required = false) MultipartFile attach) throws Exception {

		String userId = getUserID();

		ObjectMapper objectMapper = new ObjectMapper();
		AccountRequest request = objectMapper.readValue(data, AccountRequest.class);
		String girdAttach = null;
		if (attach != null && !attach.isEmpty()) {
			girdAttach = myService.saveAttach(attach);
			System.out.println(girdAttach);
		}

		Account accounts = myService.createAccount(request, girdAttach, userId);
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(accounts.getID())
				.toUri();
		System.out.println(repository.findAll());
		return ResponseEntity.created(location).body(accounts);
	}

	@DeleteMapping(value = "/{id}") // 刪除帳目
	public ResponseEntity<Account> deleteAccount(@PathVariable("id") String id) {
		myService.deleteAccount(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping // 取得帳目資訊
	public ResponseEntity<?> getAccounts(@ModelAttribute QueryParameter param) {
		try {
			String userId = getUserID();
			ArrayList<?> items = myService.getAccounts(param, userId);
			return ResponseEntity.ok(items);
		} catch (Exception ex) {
			System.out.println(ex);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of(
							"status", false,
							"message", ex.getMessage()));
		}
	}

	@GetMapping(value = "/{id}") // 取得單一帳目資訊
	public ResponseEntity<?> getAccount(@PathVariable("id") String id) {
		Account item = myService.getAccount(id);
		return ResponseEntity.ok(item);
	}

	@GetMapping("/getAttach/{id}")
	public ResponseEntity<?> getAttach(@PathVariable String id) {
		try {
			GridFSFile gridFsFile = gridFsTemplate.findOne(new org.springframework.data.mongodb.core.query.Query(
					org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id)));

			if (gridFsFile == null) {
				return ResponseEntity.notFound().build();
			}

			GridFsResource resource = gridFsTemplate.getResource(gridFsFile);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(gridFsFile.getMetadata().getString("_contentType")));

			return ResponseEntity.ok()
					.headers(headers)
					.body(resource.getInputStream().readAllBytes());

		} catch (Exception ex) {
			return ResponseEntity.status(500).body("Error retrieving file: " + ex.getMessage());
		}
	}

	@PatchMapping(value = "/{id}", consumes = "multipart/form-data") // 更新帳目
	public ResponseEntity<Account> updateProduct(
			@PathVariable("id") String id, @RequestParam("data") String data,
			@RequestParam(value = "attach", required = false) MultipartFile attach) throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();
		AccountRequest updatedFields = objectMapper.readValue(data, AccountRequest.class);
		Account updatedAccount = myService.updateAccount(id, updatedFields, attach);

		return ResponseEntity.ok(updatedAccount);
	}

	private String getUserID() throws Exception {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			String userId = userDetails.getID();

			return userId;
		} catch (Exception ex) {

			throw new RuntimeException("請重新登入");
		}
	}
}