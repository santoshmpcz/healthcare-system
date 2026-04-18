package com.app.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStorageUtil {

	@Value("${file.upload-dir}")
	private String uploadDir;

	public String saveFile(MultipartFile file) throws IOException {

		// remove spaces from filename
		String originalName = file.getOriginalFilename().replaceAll("\\s+", "_");

		// create unique filename
		String fileName = UUID.randomUUID().toString() + "_" + originalName;

		Path uploadPath = Paths.get(uploadDir);

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		Path filePath = uploadPath.resolve(fileName);

		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		return fileName;
	}
}