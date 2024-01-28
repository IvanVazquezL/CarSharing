package Test;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<String, Integer> companies = new HashMap<>();

        companies.put("Mataz", 1);

        System.out.println(companies.get("Mataz"));
    }
}
