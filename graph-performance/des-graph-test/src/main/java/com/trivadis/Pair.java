package com.trivadis;

import java.io.Serializable;

public class Pair<L,R> implements Serializable {
	private static final long serialVersionUID = -1649620640946217104L;

	private L left;
	private R right;
	
	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}
	
	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	public L left() {
		return left;
	}
	
	public R right() {
		return right;
	}

	@Override
	public String toString() {
		return "Pair [left=" + left + ", right=" + right + "]";
	}
	
}
