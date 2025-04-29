package com.example.demo.controlleur;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.DocumentationAddDto;
import com.example.demo.entity.Documentation;
import com.example.demo.servecies.DocumentationeService;

import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/documentation")
public class DocumentationController {

	@Autowired
	private DocumentationeService documentationService; // Injection du service correct

	@GetMapping()
	public List<Documentation> getAll() {
		return documentationService.getAllDocumentations();
	}
	@PreAuthorize("hasAuthority('admin')")
	@PostMapping()
	public Documentation addDocumentation(@RequestBody DocumentationAddDto doc) {
		return documentationService.addDocumentation(doc);
	}
	@PreAuthorize("hasAuthority('admin')")
	@DeleteMapping("/{id}")
	public void deleteDocumentation(@PathVariable String id) {
		documentationService.deleteDocumentation(id);
	}
	@PreAuthorize("hasAuthority('admin')")

	@PostMapping("/upload/{id}")
	public void uploadFile(@RequestParam MultipartFile file, @PathVariable String id) {
		documentationService.uploadFile(id, file);
	}
	@PreAuthorize("hasAuthority('admin')")
	@GetMapping("/download/{id}")
	public void getMethodName(@PathVariable String id, HttpServletResponse response) {
		var file = documentationService.getFile(id);
		try {
			byte[] isr = Files.readAllBytes(file.toPath());
			ByteArrayOutputStream out = new ByteArrayOutputStream(isr.length);
			out.write(isr, 0, isr.length);
			// Use 'inline' for preview and 'attachement' for download in browser.
			response.addHeader("Content-Disposition", "attachement; filename=" + file.getName());
			OutputStream os;
			os = response.getOutputStream();
			out.writeTo(os);
			os.flush();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
