package yumashish.helios.hword;

public interface HOverflow<T> {
	public T Overflow(long MAX);
	public T Overflow(long R1, long MAX);
	public T Overflow(long R1, long R2, long MAX);
}
