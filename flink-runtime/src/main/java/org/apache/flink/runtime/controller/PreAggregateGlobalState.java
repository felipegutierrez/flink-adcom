package org.apache.flink.runtime.controller;

public class PreAggregateGlobalState {
	private Long intervalMsCurrent;
	private Long intervalMsNew;
	private boolean validate;
	private boolean overloaded;

	public PreAggregateGlobalState() {
		this.validate = true;
		this.overloaded = false;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	public boolean isOverloaded() {
		return this.overloaded;
	}

	public void setOverloaded(boolean overloaded) {
		this.overloaded = overloaded;
	}

	public Long getIntervalMsCurrent() {
		return intervalMsCurrent;
	}

	public void setIntervalMsCurrent(Long intervalMsCurrent) {
		if (this.intervalMsCurrent == null) {
			this.intervalMsCurrent = intervalMsCurrent;
		} else {
			System.out.println("[PreAggregateGlobalState.controller] intervalMsCurrent already set!");
		}
	}

	public Long getIntervalMsNew() {
		return intervalMsNew;
	}

	public void incrementIntervalMsNew(long inc) {
		if (this.intervalMsNew == null && this.intervalMsCurrent != null) {
			this.intervalMsNew = this.intervalMsCurrent + inc;
		} else if (this.intervalMsNew != null) {
			System.out.println("[PreAggregateGlobalState.controller.dec] intervalMsNew already set!");
		} else if (this.intervalMsCurrent == null) {
			System.out.println("[PreAggregateGlobalState.controller.dec] intervalMsCurrent is null!");
		}
	}

	public void decrementIntervalMsNew(long dec) {
		if (this.intervalMsNew == null && this.intervalMsCurrent != null) {
			this.intervalMsNew = this.intervalMsCurrent - dec;
		} else if (this.intervalMsNew != null) {
			System.out.println("[PreAggregateGlobalState.controller.dec] intervalMsNew already set!");
		} else if (this.intervalMsCurrent == null) {
			System.out.println("[PreAggregateGlobalState.controller.dec] intervalMsCurrent is null!");
		}
	}
}
