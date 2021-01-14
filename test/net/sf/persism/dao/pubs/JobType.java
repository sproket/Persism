package net.sf.persism.dao.pubs;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.Table;

/**
 * Job descriptions
 *
 * @author Dan Howard
 * @since 5/25/12 6:06 PM
 */
@Table(value = "JobS")
public class JobType {
    /*
	[job_id] [smallint] IDENTITY(1,1) NOT NULL,
	[job_desc] [varchar](50) NOT NULL,
	[min_lvl] [tinyint] NOT NULL,
	[max_lvl] [tinyint] NOT NULL,
     */

    private int jobId;

    @Column(name = "jOb_dESC")
    private String description;

    @Column(name = "min_lvl")
    private short minLevel;

    @Column(name = "max_lvl")
    private short maxLevel;

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public short getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(short minLevel) {
        this.minLevel = minLevel;
    }

    public short getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(short maxLevel) {
        this.maxLevel = maxLevel;
    }

    @Override
    public String toString() {
        return "\nJobType{" +
                "jobId=" + jobId +
                ", description='" + description + '\'' +
                ", minLevel=" + minLevel +
                ", maxLevel=" + maxLevel +
                '}';
    }
}
