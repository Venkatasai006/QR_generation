package com.example.demo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@RestController
@RequestMapping("/auth/qr")
public class QrController {

    private final QrBlockingStore store;

    private final JwtService jwtService;

    public QrController(QrBlockingStore store, JwtService jwtService) {
        this.store = store;
        this.jwtService = jwtService;
    }

    // STEP 1: Generate QR token and return QR image
    @GetMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQr() throws Exception {
        String token = store.createToken();

        String content = "http://localhost:8080/auth/qr/approve?qrToken=" + token;

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);

        return ResponseEntity.ok()
                .header("X-QR-Token", token)
                .contentType(MediaType.IMAGE_PNG)
                .body(baos.toByteArray());
    }

    // STEP 2: Web blocks and waits
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String qrToken) throws InterruptedException {

        String accessToken = store.waitForAccessToken(qrToken);

        if (accessToken == null) {
            return ResponseEntity.status(408).body("Timeout or expired");
        }

        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    // STEP 3: Mobile approves
    @PostMapping("/approve")
    public ResponseEntity<?> approve(
            @RequestParam String qrToken,
            @RequestParam String userId
    ) throws Exception {

        String accessToken = jwtService.generateAccessToken(userId);

        store.approve(qrToken, accessToken);

        return ResponseEntity.ok("Approved");
    }
}