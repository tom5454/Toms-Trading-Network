package com.tom.trading.util;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class PredicatingHandler<T extends Resource> extends DelegatingResourceHandler<T> {
	private Predicate<T> canInsert;
	private BooleanSupplier canExtract;

	public <K> PredicatingHandler(ResourceHandler<T> delegate, K key, BiPredicate<K, T> canInsert, Predicate<K> canExtract) {
		super(delegate);
		this.canExtract = bake(key, canExtract);
		this.canInsert = bake(key, canInsert);
	}

	private static <K> BooleanSupplier bake(K key, Predicate<K> predicate) {
		if (predicate == null) return () -> false;
		return () -> predicate.test(key);
	}

	private static <K, T> Predicate<T> bake(K key, BiPredicate<K, T> predicate) {
		if (predicate == null) return (i) -> false;
		return (i) -> predicate.test(key, i);
	}

	@Override
	public int insert(int index, T resource, int amount, TransactionContext transaction) {
		if (!canInsert.test(resource))return 0;
		return super.insert(index, resource, amount, transaction);
	}

	@Override
	public int insert(T resource, int amount, TransactionContext transaction) {
		if (!canInsert.test(resource))return 0;
		return super.insert(resource, amount, transaction);
	}

	@Override
	public int extract(int index, T resource, int amount, TransactionContext transaction) {
		if (!canExtract.getAsBoolean())return 0;
		return super.extract(index, resource, amount, transaction);
	}

	@Override
	public int extract(T resource, int amount, TransactionContext transaction) {
		if (!canExtract.getAsBoolean())return 0;
		return super.extract(resource, amount, transaction);
	}

	@Override
	public boolean isValid(int index, T resource) {
		if (!canInsert.test(resource))return false;
		return super.isValid(index, resource);
	}

	@Override
	public long getCapacityAsLong(int index, T resource) {
		if (!canInsert.test(resource))return 0L;
		return super.getCapacityAsLong(index, resource);
	}
}
