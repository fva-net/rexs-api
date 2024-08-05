package info.rexs.model.jaxb;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.Arrays;
import java.util.Locale;

/**
 * Adapter class to convert between `String` and `Locale` for XML binding.
 * <p>
 * This adapter is used to marshal and unmarshal `Locale` objects to and from their language code representation in XML.
 */
public class LocaleAdapter extends XmlAdapter<String, Locale> {

    @Override
    public String marshal(final Locale v)
    {
        if(v == null) {
            return null;
        }
        return v.getLanguage();
    }

    @Override
    public Locale unmarshal(final String v)
    {
        // split the string by underscore
        final String[] parts = v.split("_");

        // check if the first part is a valid language code
        if (Arrays.asList(Locale.getISOLanguages()).contains(parts[0])) {
            return Locale.forLanguageTag(parts[0]);
        }

        // return null if no matching Locale is found
        return null;
    }
}