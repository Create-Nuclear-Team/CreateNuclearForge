package net.nuclearteam.createnuclear.api.multiblock;

import lib.multiblock.impl.IMultiBlockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class MultiBlockManagerBeta <T> {
    private final ArrayList<BlockPattern<T>> structures = new ArrayList<>();

    public MultiBlockManagerBeta() {}

    public void register(String id, T data, IMultiBlockPattern blockPattern) {
        structures.add(new BlockPattern<>(id, data, blockPattern));
    }

    public BlockPattern<T> findStructure(Level level, BlockPos pos, ReactorControllerBlockEntity entity) {
        List<Direction> directions = new ArrayList<>();
        directions.add(Direction.NORTH);
        directions.add(Direction.WEST);
        directions.add(Direction.EAST);
        directions.add(Direction.SOUTH);
        record id(String id, int size) {};
        List<id> ids = List.of(
                new id("createnuclear:reactor5x5", 5),
                new id("createnuclear:reactor7x7", 7),
                new id("createnuclear:reactor9x9",9)
        );

        for (Direction direction : directions) {
            for (BlockPattern<T> structure : structures) {
                var result = structure.structure().matches(level, pos, direction);
                if (result){
                    entity.reactorFacing = direction.getCounterClockWise().getName();
                    entity.reactorSize = ids.stream()
                            .filter(item -> item.id().equals(structure.id()))
                            .findFirst()
                            .map(id::size)
                            .orElse(-1);
                    return structure;
                }
            }
        }

        return null;
    }
}
