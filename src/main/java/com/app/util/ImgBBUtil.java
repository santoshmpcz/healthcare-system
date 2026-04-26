package com.app.util;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImgBBUtil {

	@Value("${imgbb.api.key}")
	private String apiKey;

	public String uploadImage(MultipartFile file) throws IOException {

		if (file == null || file.isEmpty()) {
			throw new RuntimeException("File is empty");
		}

		if (apiKey == null || apiKey.isBlank()) {
			throw new RuntimeException("ImgBB API key is missing");
		}

		String url = "https://api.imgbb.com/1/upload?key=" + apiKey;

		String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

		RestTemplate restTemplate = new RestTemplate();

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("image", base64Image);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		// ✅ FULLY TYPE-SAFE (NO WARNING)
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
			throw new RuntimeException("Failed to upload image to ImgBB");
		}

		Map<String, Object> responseBody = response.getBody();

		Object dataObj = responseBody.get("data");

		if (!(dataObj instanceof Map<?, ?> dataMap)) {
			throw new RuntimeException("Invalid response from ImgBB");
		}

		Object urlObj = dataMap.get("url");

		if (urlObj == null) {
			throw new RuntimeException("Image URL not found");
		}

		return urlObj.toString();
	}
}