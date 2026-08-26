package lib.multiblock.impl;

import lib.multiblock.SimpleMultiBlockPattern;

public interface IMultiBlockPatternBuilder {
    < T extends IMultiBlockPattern> T build(IPatternBuilder<T> builder);

    default <T extends IMultiBlockPattern> T build() {
        return (T) build(SimpleMultiBlockPattern::new);
    }
}
