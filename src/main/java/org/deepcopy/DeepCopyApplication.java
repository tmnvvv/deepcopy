package org.deepcopy;

import java.util.ArrayList;
import java.util.List;

public class DeepCopyApplication {

    public static void main(String[] args) {

        List<String> books = new ArrayList<>(List.of("Book A", "Book B"));
        Man original = new Man("John", 30, books);

        Man copy = (Man) CopyUtils.deepCopy(original);

        System.out.println("Original: " + original.getFavoriteBooks());
        System.out.println("Copy: " + copy.getFavoriteBooks());

        original.getFavoriteBooks().add("Book C");

        System.out.println("After modification:");
        System.out.println("Original: " + original.getFavoriteBooks());
        System.out.println("Copy: " + copy.getFavoriteBooks());
    }
}
