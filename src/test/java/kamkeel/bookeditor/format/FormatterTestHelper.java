package kamkeel.bookeditor.format;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;


public final class FormatterTestHelper {
    private FormatterTestHelper() {
    }

    public static Collection<Object[]> allFormatterSuppliers() {
        return Arrays.asList(new Object[][]{
            {"Standalone", (Supplier<BookFormatter>) StandaloneBookFormatter::new},
            {"HexText", (Supplier<BookFormatter>) () -> new HexTextFormatter(() -> false, () -> false)},
            {"HexText Ampersand", (Supplier<BookFormatter>) () -> new HexTextFormatter(() -> true, () -> false)}
        });
    }
}
