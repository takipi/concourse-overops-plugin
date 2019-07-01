package com.overops.plugins.step;

import com.overops.plugins.Context;
import com.overops.plugins.core.Step;
import com.overops.plugins.model.QueryOverOps;
import com.overops.plugins.utils.StringUtils;
import com.takipi.common.util.Pair;
import org.fusesource.jansi.Ansi;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PreparationStep extends Step<String[], QueryOverOps> {

    public PreparationStep(Context context) {
        super(context);
    }

    @Override
    public QueryOverOps run(String[] args) {
        context.getOutputStream().println("OverOps [Step 1/3]: Preparation data...", Ansi.Color.YELLOW);
        QueryOverOps query = QueryOverOps.mapToObject(firstStep(args));
        validation(query);
        return query;
    }

    private Map<String, String> firstStep(String[] args) {
        return Arrays.stream(args).filter(e -> e.startsWith("--") && e.contains("=")).map(e -> e.substring(2))
                .map(e -> {
                    int i = e.indexOf("=");
                    return Pair.of(e.substring(0, i), e.substring(++i));
                }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private void validation(QueryOverOps query) {
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
