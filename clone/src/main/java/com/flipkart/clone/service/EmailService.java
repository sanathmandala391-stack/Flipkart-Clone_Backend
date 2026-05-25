//package com.flipkart.clone.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    // ── Send OTP email ───────────────────────────────────────────
//    public void sendOtpEmail(String toEmail, String otp) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject("Your Flipkart OTP Code");
//        message.setText(
//                "Your OTP is: " + otp + "\n\n" +
//                        "This OTP is valid for 5 minutes.\n" +
//                        "Do not share it with anyone.\n\n" +
//                        "Team Flipkart"
//        );
//        mailSender.send(message);
//    }
//
//    // ── Send order confirmation email ────────────────────────────
//    public void sendOrderConfirmation(String toEmail,
//                                      String orderId,
//                                      String amount) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject("Order Confirmed! #" + orderId);
//        message.setText(
//                "Your order #" + orderId + " has been confirmed.\n" +
//                        "Amount paid: ₹" + amount + "\n\n" +
//                        "Track your order in the app.\n\n" +
//                        "Team Flipkart"
//        );
//        mailSender.send(message);
//    }
//
//    // ── Send vendor approval email ───────────────────────────────
//    public void sendVendorApprovalEmail(String toEmail, String shopName) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject("Your Seller Account is Approved!");
//        message.setText(
//                "Congratulations! Your shop '" + shopName + "' has been approved.\n" +
//                        "You can now start listing products.\n\n" +
//                        "Team Flipkart"
//        );
//        mailSender.send(message);
//    }
//}



package com.flipkart.clone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // ── Send OTP email ───────────────────────────────────────────
    public void sendOtpEmail(String toEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("EkartServices.com");

        message.setTo(toEmail);

        message.setSubject("Your Flipkart OTP Code");

        message.setText(
                "Your OTP is: " + otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n" +
                        "Do not share it with anyone.\n\n" +
                        "Team Flipkart"
        );

        mailSender.send(message);
    }

    // ── Send order confirmation email ────────────────────────────
    public void sendOrderConfirmation(String toEmail,
                                      String orderId,
                                      String amount) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("EkartServices.com");

        message.setTo(toEmail);

        message.setSubject("Order Confirmed! #" + orderId);

        message.setText(
                "Your order #" + orderId + " has been confirmed.\n" +
                        "Amount paid: ₹" + amount + "\n\n" +
                        "Track your order in the app.\n\n" +
                        "Team Flipkart"
        );

        mailSender.send(message);
    }

    // ── Send vendor approval email ───────────────────────────────
    public void sendVendorApprovalEmail(String toEmail,
                                        String shopName) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("EkartServices.com");

        message.setTo(toEmail);

        message.setSubject("Your Seller Account is Approved!");

        message.setText(
                "Congratulations! Your shop '" + shopName + "' has been approved.\n" +
                        "You can now start listing products.\n\n" +
                        "Team Flipkart"
        );

        mailSender.send(message);
    }
}