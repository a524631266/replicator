package com.booking.replication.sql;

import com.booking.replication.Configuration;
import com.booking.replication.sql.exception.QueryInspectorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bosko on 8/26/16.
 */
public class QueryInspector {

    private final Pattern isDDLTablePattern;
    private final Pattern isDDLViewPattern;
    private final Pattern isBeginPattern;
    private final Pattern isCommitPattern;
    private final Pattern isPseudoGTIDPattern;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryInspector.class);

    public QueryInspector(String gtidPattern) {

        this.isDDLTablePattern = Pattern.compile(QueryPatterns.isDDLTable, Pattern.CASE_INSENSITIVE);
        this.isDDLViewPattern = Pattern.compile(QueryPatterns.isDDLView, Pattern.CASE_INSENSITIVE);
        this.isBeginPattern      = Pattern.compile(QueryPatterns.isBEGIN, Pattern.CASE_INSENSITIVE);
        this.isCommitPattern     = Pattern.compile(QueryPatterns.isCOMMIT, Pattern.CASE_INSENSITIVE);
        this.isPseudoGTIDPattern = Pattern.compile(gtidPattern, Pattern.CASE_INSENSITIVE);

    }

    public boolean isDDLTable(String querySQL) {

        // optimization
        if (querySQL.equals("BEGIN")) {
            return false;
        }

        Matcher matcher = isDDLTablePattern.matcher(querySQL);

        return matcher.find();
    }

    public boolean isDDLView(String querySQL) {

        // optimization
        if (querySQL.equals("BEGIN")) {
            return false;
        }

        Matcher matcher = isDDLViewPattern.matcher(querySQL);

        return matcher.find();
    }

    public boolean isBegin(String querySQL, boolean isDDL) {

        boolean hasBegin;

        // optimization
        if (querySQL.equals("COMMIT")) {
            hasBegin = false;
        } else {
            Matcher matcher = isBeginPattern.matcher(querySQL);
            hasBegin = matcher.find();
        }

        return (hasBegin && !isDDL);
    }

    public boolean isCommit(String querySQL, boolean isDDL) {

        boolean hasCommit;

        // optimization
        if (querySQL.equals("BEGIN")) {
            hasCommit = false;
        } else {
            Matcher matcher = isCommitPattern.matcher(querySQL);
            hasCommit = matcher.find();
        }
        return (hasCommit && !isDDL);
    }

    public boolean isPseudoGTID(String querySQL) {

        // optimization
        if (querySQL.equals("BEGIN") || querySQL.equals("COMMIT")) {
            return false;
        }

        Matcher matcher = isPseudoGTIDPattern.matcher(querySQL);

        boolean found = matcher.find();

        return found;
    }

    public String extractPseudoGTID(String querySQL) throws QueryInspectorException {

        Matcher matcher = isPseudoGTIDPattern.matcher(querySQL);

        boolean found = matcher.find();
        if (found) {
            if (!(matcher.groupCount() == 1)) {
                throw new QueryInspectorException("Invalid PseudoGTID query. Could not extract PseudoGTID from: " + querySQL);
            }
            String pseudoGTID = matcher.group(0);
            return  pseudoGTID;

        } else {
            throw new QueryInspectorException("Invalid PseudoGTID query. Could not extract PseudoGTID from: " + querySQL);
        }
    }
}
