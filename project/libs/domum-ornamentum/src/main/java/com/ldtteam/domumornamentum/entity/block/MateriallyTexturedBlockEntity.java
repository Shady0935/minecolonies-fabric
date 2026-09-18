package com.ldtteam.domumornamentum.entity.block;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.util.MaterialTextureDataUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.ldtteam.domumornamentum.entity.block.ModBlockEntityTypes.MATERIALLY_TEXTURED;

public class MateriallyTexturedBlockEntity extends BlockEntity implements IMateriallyTexturedBlockEntity
{

    private MaterialTextureData textureData = MaterialTextureData.EMPTY;

    public MateriallyTexturedBlockEntity(BlockPos pos, BlockState state)
    {
        super(MATERIALLY_TEXTURED.get(), pos, state);
    }

    @Override
    public void updateTextureDataWith(final MaterialTextureData materialTextureData)
    {
        this.textureData = materialTextureData;
        if (this.textureData.isEmpty()) {
            this.textureData = MaterialTextureDataUtil.generateRandomTextureDataFrom(this.getBlockState().getBlock());
        }

        this.setChanged();
        if (this.level != null)
        {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag()
    {
        return this.saveWithId();
    }

    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        this.load(Objects.requireNonNull(packet.getTag()));
    }

    public void handleUpdateTag(final CompoundTag tag)
    {
        this.load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.put("textureData", textureData.serializeNBT());
    }

    @Override
    public void load(@NotNull final CompoundTag nbt)
    {
        super.load(nbt);

        this.textureData = new MaterialTextureData();
        if (nbt.contains("textureData", Tag.TAG_COMPOUND))
        {
            this.textureData.deserializeNBT(nbt.getCompound("textureData"));
            if (getBlockState().getBlock() instanceof IMateriallyTexturedBlock materiallyTexturedBlock)
            {
                final List<ResourceLocation> validKeys = new ArrayList<>();
                materiallyTexturedBlock.getComponents().forEach(key -> validKeys.add(key.getId()));
                final Map<ResourceLocation, Block> textureMap = new HashMap<>();
                for (Map.Entry<ResourceLocation, Block> entry : this.textureData.getTexturedComponents().entrySet())
                {
                    if (validKeys.contains(entry.getKey()))
                    {
                        textureMap.put(entry.getKey(), entry.getValue());
                    }
                }
                this.textureData = new MaterialTextureData(textureMap);
            }
        }

        this.setChanged();
    }

    @Override
    @NotNull
    public MaterialTextureData getTextureData()
    {
        return textureData;
    }
}
