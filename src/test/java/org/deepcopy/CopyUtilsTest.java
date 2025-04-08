package org.deepcopy;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CopyUtilsTest {

    @Test
    void testDeepCopySimpleManObject() {
        Man original = new Man("John", 30, new ArrayList<>(List.of("Book A", "Book B")));
        Man copy = CopyUtils.deepCopy(original);

        assertNotSame(original, copy);
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getAge(), copy.getAge());
        assertEquals(original.getFavoriteBooks(), copy.getFavoriteBooks());

        original.setName("Jane");
        original.setAge(35);
        original.getFavoriteBooks().add("Book C");

        assertNotEquals(original.getName(), copy.getName());
        assertNotEquals(original.getAge(), copy.getAge());
        assertFalse(copy.getFavoriteBooks().contains("Book C"));
    }

    @Test
    void testDeepCopyManWithEmptyFavoriteBooks() {
        Man original = new Man("Alice", 25, new ArrayList<>());
        Man copy = CopyUtils.deepCopy(original);

        assertNotSame(original, copy);
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getAge(), copy.getAge());
        assertEquals(original.getFavoriteBooks(), copy.getFavoriteBooks());

        original.getFavoriteBooks().add("Book D");
        assertFalse(copy.getFavoriteBooks().contains("Book D"));
    }

    @Test
    void testDeepCopyManWithNullFavoriteBooks() {
        Man original = new Man("Bob", 40, null);
        Man copy = CopyUtils.deepCopy(original);

        assertNotSame(original, copy);
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getAge(), copy.getAge());
        assertNull(copy.getFavoriteBooks());

        original.setName("Robert");
        assertNotEquals(original.getName(), copy.getName());
    }

    @Test
    void testDeepCopyManAndModifyFavoriteBooks() {
        List<String> books = new ArrayList<>(List.of("Book X", "Book Y"));
        Man original = new Man("Charlie", 28, books);
        Man copy = CopyUtils.deepCopy(original);

        assertNotSame(original, copy);
        assertNotSame(original.getFavoriteBooks(), copy.getFavoriteBooks());
        assertEquals(original.getFavoriteBooks(), copy.getFavoriteBooks());

        original.getFavoriteBooks().add("Book Z");

        assertFalse(copy.getFavoriteBooks().contains("Book Z"));
        assertTrue(original.getFavoriteBooks().contains("Book Z"));
    }


    @Test
    void testDeepCopyManWithCircularReferences() {
        Man original = new Man("Dave", 45, new ArrayList<>(List.of("Book A", "Book B")));
        original.getFavoriteBooks().add(original.getName());
        Man copy = CopyUtils.deepCopy(original);

        assertNotSame(original, copy);
        assertNotSame(original.getFavoriteBooks(), copy.getFavoriteBooks());
    }

    @Test
    void testDeepCopyManWithComplexFavoriteBooks() {
        List<String> books = new ArrayList<>(List.of("Book A", "Book B", "Book C"));
        Man original = new Man("Eve", 32, books);
        Man copy = CopyUtils.deepCopy(original);

        assertNotSame(original, copy);
        assertNotSame(original.getFavoriteBooks(), copy.getFavoriteBooks());
        assertEquals(original.getFavoriteBooks(), copy.getFavoriteBooks());

        original.getFavoriteBooks().add("Book D");

        assertFalse(copy.getFavoriteBooks().contains("Book D"));
        assertTrue(original.getFavoriteBooks().contains("Book D"));
    }

    @Test
    void testDeepCopyManWithNestedObjects() {
        List<String> books = new ArrayList<>(List.of("Book E", "Book F"));
        Man original = new Man("Frank", 50, books);
        Man copy = CopyUtils.deepCopy(original);

        assertNotSame(original, copy);
        assertNotSame(original.getFavoriteBooks(), copy.getFavoriteBooks());
        assertEquals(original.getFavoriteBooks(), copy.getFavoriteBooks());

        original.setName("Francis");
        original.getFavoriteBooks().add("Book G");

        assertNotEquals(original.getName(), copy.getName());
        assertFalse(copy.getFavoriteBooks().contains("Book G"));
    }

    @Test
    void testDeepCopyPersonRecord() {
        List<String> books = new ArrayList<>(List.of("Book 1", "Book 2"));
        PersonRecord original = new PersonRecord("Alice", 30, books);
        PersonRecord copy = CopyUtils.deepCopy(original);

        assertNotSame(original, copy);
        assertEquals(original.name(), copy.name());
        assertEquals(original.age(), copy.age());
        assertEquals(original.favoriteBooks(), copy.favoriteBooks());

        original.favoriteBooks().add("Book 3");

        assertFalse(copy.favoriteBooks().contains("Book 3"));
        assertTrue(original.favoriteBooks().contains("Book 3"));
    }
}
