package org.fenixedu.bennu.core.domain.groups;

import com.google.gson.JsonParser;
import org.fenixedu.bennu.core.annotation.GroupArgumentParser;
import org.fenixedu.bennu.core.groups.ArgumentParser;
import org.fenixedu.commons.i18n.LocalizedString;

@GroupArgumentParser
public class LocalizedStringGroupArgumentParser implements ArgumentParser<LocalizedString> {

    @Override public LocalizedString parse(String s) {
        return LocalizedString.fromJson(new JsonParser().parse(s));
    }

    @Override public String serialize(LocalizedString localizedString) {
        return localizedString.json().toString();
    }

    @Override public Class<LocalizedString> type() {
        return LocalizedString.class;
    }
}