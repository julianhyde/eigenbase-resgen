// This class is generated. Do NOT modify it, or
// add it to source control.

package happy;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import org.eigenbase.resgen.*;

/**
 * This class was generated
 * by class org.eigenbase.resgen.ResourceGen
 * from /home/jhyde/open1/thirdparty/resgen/example/source/happy/BirthdayResource.xml
 * on Tue Jul 17 20:52:06 PDT 2012.
 * It contains a list of messages, and methods to
 * retrieve and format those messages.
 */

public class BirthdayResource extends org.eigenbase.resgen.ShadowResourceBundle {
    public BirthdayResource() throws IOException {
    }
    private static final String baseName = "happy.BirthdayResource";
    /**
     * Retrieves the singleton instance of {@link BirthdayResource}. If
     * the application has called {@link #setThreadLocale}, returns the
     * resource for the thread's locale.
     */
    public static synchronized BirthdayResource instance() {
        return (BirthdayResource) instance(baseName, getThreadOrDefaultLocale(), ResourceBundle.getBundle(baseName, getThreadOrDefaultLocale()));
    }
    /**
     * Retrieves the instance of {@link BirthdayResource} for the given locale.
     */
    public static synchronized BirthdayResource instance(Locale locale) {
        return (BirthdayResource) instance(baseName, locale, ResourceBundle.getBundle(baseName, locale));
    }

    /**
     * <code>HappyBirthday</code> is '<code>Happy Birthday, {0}! You don&#39;&#39;t look {1,number}.</code>'
     */
    public static final org.eigenbase.resgen.ResourceDefinition HappyBirthday = new org.eigenbase.resgen.ResourceDefinition("HappyBirthday", "Happy Birthday, {0}! You don''t look {1,number}.");
    public String getHappyBirthday(String p0, Number p1) {
        return HappyBirthday.instantiate(this, new Object[] {p0, p1}).toString();
    }

    /**
     * <code>TooYoung</code> is '<code>{0} has not been born yet.</code>'
     */
    public static final org.eigenbase.resgen.ResourceDefinition TooYoung = new org.eigenbase.resgen.ResourceDefinition("TooYoung", "{0} has not been born yet.");
    public String getTooYoung(String p0) {
        return TooYoung.instantiate(this, new Object[] {p0}).toString();
    }
    public RuntimeException newTooYoung(String p0) {
        return new RuntimeException(getTooYoung(p0));
    }
    public RuntimeException newTooYoung(String p0, Throwable err) {
        return new RuntimeException(getTooYoung(p0), err);
    }

    /**
     * <code>ShouldGiveWarning</code> is '<code>You don&#39;t look {1,number}, {0}!</code>'
     */
    public static final org.eigenbase.resgen.ResourceDefinition ShouldGiveWarning = new org.eigenbase.resgen.ResourceDefinition("ShouldGiveWarning", "You don't look {1,number}, {0}!");
    public String getShouldGiveWarning() {
        return ShouldGiveWarning.instantiate(this, emptyObjectArray).toString();
    }
    public RuntimeException newShouldGiveWarning() {
        return new RuntimeException(getShouldGiveWarning());
    }
    public RuntimeException newShouldGiveWarning(Throwable err) {
        return new RuntimeException(getShouldGiveWarning(), err);
    }

    /**
     * <code>WithMarkup</code> is '<code>This &lt;tag is=&quot;not&quot;&gt;very&lt;/kosher&gt;.</code>'
     */
    public static final org.eigenbase.resgen.ResourceDefinition WithMarkup = new org.eigenbase.resgen.ResourceDefinition("WithMarkup", "This <tag is=\"not\">very</kosher>.");
    public String getWithMarkup() {
        return WithMarkup.instantiate(this, emptyObjectArray).toString();
    }

}
