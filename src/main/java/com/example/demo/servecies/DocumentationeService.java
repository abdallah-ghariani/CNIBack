package com.example.demo.servecies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.DocumentationAddDto;
import com.example.demo.entity.Documentation;
import com.example.demo.repository.DocumentationRepository;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentationeService {

	@Value("${documentation.save.path}")
	private String docmentationSavePath = "docs";

	@Autowired
	private DocumentationRepository documentationRepository;

	public List<Documentation> getAllDocumentations() {
		return documentationRepository.findAll();
	}

	public Documentation getById(String id) {
		return documentationRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "documentation not found"));
	}

	public Documentation addDocumentation(DocumentationAddDto dto) {
		var doc = new Documentation();
		doc.setTitle(dto.getTitle());
		doc.setDescription(dto.getDescription());
		doc.setUrl(dto.getUrl());
		return documentationRepository.save(doc);
	}

	public void uploadFile(String id, MultipartFile file) {
		String fileName = UUID.randomUUID().toString() + "." + file.getOriginalFilename();
		var doc = getById(id);
		doc.setFileName(fileName);
		try {
			Files.copy(file.getInputStream(), Path.of(docmentationSavePath, fileName));
		} catch (IllegalStateException | IOException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
		documentationRepository.save(doc);
	}

	public void deleteDocumentation(String id) {
		var doc = getById(id);
		try {
			Files.deleteIfExists(Path.of(docmentationSavePath, doc.getFileName()));
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
		documentationRepository.delete(doc);

	}

	public File getFile(String id) {
		var doc = getById(id);
		var path = Path.of(docmentationSavePath, doc.getFileName()).toString();
		var file = new File(path);
		if (file.exists() || file.canRead()) 
			return file;
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file not found");

	}
}
