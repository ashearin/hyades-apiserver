/*
 * This file is generated by jOOQ.
 */
package org.dependencytrack.persistence.jooq.generated.tables.records;


import javax.annotation.processing.Generated;

import org.dependencytrack.persistence.jooq.generated.tables.ProjectAccessTeams;
import org.jooq.Record2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.20.4",
        "schema version:v5.6.0-28"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class ProjectAccessTeamsRecord extends UpdatableRecordImpl<ProjectAccessTeamsRecord> {

    private static final long serialVersionUID = -1553693119;

    /**
     * Setter for <code>PROJECT_ACCESS_TEAMS.PROJECT_ID</code>.
     */
    public ProjectAccessTeamsRecord setProjectId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>PROJECT_ACCESS_TEAMS.PROJECT_ID</code>.
     */
    public Long getProjectId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>PROJECT_ACCESS_TEAMS.TEAM_ID</code>.
     */
    public ProjectAccessTeamsRecord setTeamId(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>PROJECT_ACCESS_TEAMS.TEAM_ID</code>.
     */
    public Long getTeamId() {
        return (Long) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<Long, Long> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ProjectAccessTeamsRecord
     */
    public ProjectAccessTeamsRecord() {
        super(ProjectAccessTeams.PROJECT_ACCESS_TEAMS);
    }

    /**
     * Create a detached, initialised ProjectAccessTeamsRecord
     */
    public ProjectAccessTeamsRecord(Long projectId, Long teamId) {
        super(ProjectAccessTeams.PROJECT_ACCESS_TEAMS);

        setProjectId(projectId);
        setTeamId(teamId);
        resetTouchedOnNotNull();
    }
}
