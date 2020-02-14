package com.overops.plugins.step;

import com.overops.plugins.Context;
import com.overops.plugins.core.Step;
import com.overops.plugins.model.QueryOverConfig;
import com.takipi.common.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PreparationStep extends Step<String[], QueryOverConfig> {

    public PreparationStep(Context context) {
        super(context);
    }

    @Override
    public QueryOverConfig run(String[] args) {
        context.getOutputStream().println("OverOps [Step 1/3]: Preparation data...", Ansi.Color.MAGENTA);
        QueryOverConfig query = QueryOverConfig.mapToObject(firstStep(args));
        validation(query);
        return query;
    }

    private Map<String, String> firstStep(String[] args) {
        final String parameterDeclaration = "--";
        return Arrays.stream(args).filter(e -> e.startsWith(parameterDeclaration) && e.contains("="))
                .map(e -> e.substring(parameterDeclaration.length()))
                .map(e -> {
                    int i = e.indexOf("=");
                    return Pair.of(e.substring(0, i), e.substring(++i));
                }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private void validation(QueryOverConfig query) {
        if (StringUtils.isEmpty(query.getOverOpsURL())) {
            throw new IllegalArgumentException("OverOps url can not be empty.");
        }
        if (StringUtils.isEmpty(query.getOverOpsSID())) {
            throw new IllegalArgumentException("Environment id can not be empty.");
        }
        if (StringUtils.isEmpty(query.getOverOpsAPIKey())) {
            throw new IllegalArgumentException("Token can not be empty.");
        }
    }
}
