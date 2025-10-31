package kamkeel.bookeditor.format;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class BookFormatterSharedTest {
    private final Supplier<BookFormatter> formatterSupplier;
    private BookFormatter formatter;

    public BookFormatterSharedTest(String name, Supplier<BookFormatter> formatterSupplier) {
        this.formatterSupplier = formatterSupplier;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return FormatterTestHelper.allFormatterSuppliers();
    }

    @Before
    public void setUpFormatter() {
        this.formatter = formatterSupplier.get();
    }

    @Test
    public void detectFormattingCodeLengthRecognisesSectionSign() {
        assertThat(formatter.detectFormattingCodeLength("§aHello", 0) >= 2, is(true));
    }

    @Test
    public void detectFormattingCodeLengthReturnsZeroWhenAbsent() {
        assertThat(formatter.detectFormattingCodeLength("NoCode", 0), is(0));
    }

    @Test
    public void findFormattingCodeStartFindsSectionSign() {
        String first = "§aHello";
        int firstEnd = first.indexOf('H');
        assertThat(formatter.findFormattingCodeStart(first, firstEnd), is(0));

        String second = "§l§oBold";
        assertThat(formatter.findFormattingCodeStart(second, 4), is(2));
    }

    @Test
    public void sanitizeFormattingRemovesDanglingMarkers() {
        String sanitized = formatter.sanitizeFormatting("Line§");
        assertThat(sanitized, is("Line"));
    }

    @Test
    public void getActiveFormattingAccumulatesCodes() {
        assertThat(formatter.getActiveFormatting("§aGreen §lBold"), is("§a§l"));
    }

    @Test
    public void stripColorCodesRemovesSectionFormatting() {
        assertThat(formatter.stripColorCodes("§aColour"), is("Colour"));
    }

    @Test
    public void getFormatFromStringReturnsLatestFormatting() {
        assertThat(formatter.getFormatFromString("§aGreen§lBold"), is("§a§l"));
    }
}
