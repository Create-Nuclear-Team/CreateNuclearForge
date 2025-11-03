package lib.multiblock.manager;

import lib.multiblock.impl.IMultiBlockPattern;

public record RegisteredMultiBlockPattern<T> (String ID, T data, IMultiBlockPattern pattern) {
}
