package com.chocopepper.chococam.model;


public class AppInfo {

	public static final String TABLE_NAME = AppInfo.class.getSimpleName();
	public static final String TABLE_NAME_RUNNING = TABLE_NAME + "_Running";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String PACKAGENAME = "packagaName";
	public static final String RUNCOUNT = "runCount";
	public static final String ICON = "icon";
	public static final String LASTRUNNINGTIME = "lastRunningTime";
	
	
	private long id;
	private String name;
    private String packagaName;
    private long runCount;
    private long lastRunningTime = -1;
    private byte[] icon;
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the packagaName
	 */
	public String getPackagaName() {
		return packagaName;
	}
	/**
	 * @param packagaName the packagaName to set
	 */
	public void setPackagaName(String packagaName) {
		this.packagaName = packagaName;
	}
	/**
	 * @return the runCount
	 */
	public long getRunCount() {
		return runCount;
	}
	/**
	 * @param runCount the runCount to set
	 */
	public void setRunCount(long runCount) {
		this.runCount = runCount;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AppInfo [id=" + id + ", name=" + name + ", packagaName=" + packagaName
				+ ", runCount=" + runCount + "]";
	}
	/**
	 * @param lastRunningTime the lastRunningTime to set
	 */
	public void setLastRunningTime(long lastRunningTime) {
		this.lastRunningTime = lastRunningTime;
	}
	/**
	 * @return the lastRunningTime
	 */
	public long getLastRunningTime() {
		if (lastRunningTime == -1)
			lastRunningTime = System.currentTimeMillis();
		return lastRunningTime;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(byte[] icon) {
		this.icon = icon;
	}
	/**
	 * @return the icon
	 */
	public byte[] getIcon() {
		return icon;
	}
}
