package com.be.server.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class RandomNumberGenerator {


        private static final Random RANDOM = new Random();

        public static String generateRoomType() {
            int number = RANDOM.nextInt(10000); // từ 0 - 9999
            return String.format("LP-%04d", number); // luôn 4 chữ số, ví dụ MP-0123
        }

    public String randomToString(String name, int soLuong) {
        Random random = new Random();
        int randomNumber = random.nextInt(soLuong);
        return name + randomNumber;
    }
    private static int previousRandomNumber = 1;

    public String randomToString(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name).append(previousRandomNumber);
        previousRandomNumber++;
        return stringBuilder.toString();
    }

    public  int generateRandom6DigitNumber() {

        Random random = new Random();
        int minRange = 100000; // Số nhỏ nhất có 6 chữ số
        int maxRange = 999999; // Số lớn nhất có 6 chữ số

        return random.nextInt(maxRange - minRange + 1) + minRange;
    }
    public  String randomPassword() {
        String password =  RandomStringUtils.random(8,true,true);

        return password;
    }

    public static void main(String[] args) {


        System.out.println(new RandomNumberGenerator().randomToString("KM"));
        System.out.println(new RandomNumberGenerator().randomToString("KM"));
        System.out.println(new RandomNumberGenerator().randomPassword());
    }
}