package com.ldtteam.multipiston.client;

import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.multipiston.MultiPiston;
import com.ldtteam.multipiston.TileEntityMultiPiston;
import com.ldtteam.structurize.api.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.ldtteam.multipiston.TileEntityMultiPiston.DEFAULT_RANGE;
import static com.ldtteam.multipiston.TileEntityMultiPiston.DEFAULT_SPEED;
import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;

/** Client settings for the Multi-Piston's input face, output face, range and speed. */
public final class WindowMultiPiston extends AbstractWindowSkeleton
{
    private static final List<String> COLORS = List.of("Yellow", "Orange", "Blue", "Green", "Red", "Purple");
    private static final List<Direction> DIRECTIONS = List.of(UP, DOWN, NORTH, EAST, SOUTH, WEST);
    public static final String INPUT_RANGE_NAME = "range";
    public static final String INPUT_SPEED = "speed";
    public static final String BUTTON_CONFIRM = "confirm";

    private final BlockPos pos;
    private final TextField inputRange;
    private final TextField inputSpeed;
    private DropDownList outputDropdown;
    private DropDownList inputDropdown;
    private Direction input = UP;
    private Direction output = DOWN;

    public WindowMultiPiston(@Nullable final BlockPos pos)
    {
        super(MultiPiston.MOD_ID + ":gui/windowmultipiston.xml");
        this.pos = pos;
        inputRange = findPaneOfTypeByID(INPUT_RANGE_NAME, TextField.class);
        inputSpeed = findPaneOfTypeByID(INPUT_SPEED, TextField.class);
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        initDropDowns();
    }

    @Override
    public void onOpened()
    {
        final Minecraft minecraft = Minecraft.getInstance();
        if (pos == null || minecraft.level == null)
        {
            close();
            return;
        }
        final BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
        if (!(blockEntity instanceof TileEntityMultiPiston multiPiston))
        {
            close();
            return;
        }

        initDropDowns();
        inputRange.setText(Integer.toString(multiPiston.getRange()));
        inputSpeed.setText(Integer.toString(multiPiston.getSpeed()));
        input = multiPiston.getInput();
        output = multiPiston.getOutput();
        inputDropdown.setSelectedIndex(DIRECTIONS.indexOf(input));
        outputDropdown.setSelectedIndex(DIRECTIONS.indexOf(output));
    }

    private void initDropDowns()
    {
        outputDropdown = findPaneOfTypeByID("output", DropDownList.class);
        outputDropdown.setHandler(this::toggleOutput);
        outputDropdown.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return COLORS.size();
            }

            @Override
            public String getLabel(final int index)
            {
                return COLORS.get(index);
            }
        });

        inputDropdown = findPaneOfTypeByID("input", DropDownList.class);
        inputDropdown.setHandler(this::toggleInput);
        inputDropdown.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return COLORS.size();
            }

            @Override
            public String getLabel(final int index)
            {
                return COLORS.get(index);
            }
        });
    }

    private void toggleInput(final DropDownList list)
    {
        final Direction selected = directionAt(list.getSelectedIndex());
        if (selected == output)
        {
            restoreSelection(list, input);
            return;
        }
        input = selected;
    }

    private void toggleOutput(final DropDownList list)
    {
        final Direction selected = directionAt(list.getSelectedIndex());
        if (selected == input)
        {
            restoreSelection(list, output);
            return;
        }
        output = selected;
    }

    private Direction directionAt(final int index)
    {
        return DIRECTIONS.get(Math.max(0, Math.min(index, DIRECTIONS.size() - 1)));
    }

    private void restoreSelection(final DropDownList list, final Direction direction)
    {
        Utils.playErrorSound(Minecraft.getInstance().player);
        if (Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.displayClientMessage(
              Component.translatable("com.ldtteam.multipiston.equalpos"), false);
        }
        list.setSelectedIndex(DIRECTIONS.indexOf(direction));
    }

    private void confirmClicked()
    {
        int range = DEFAULT_RANGE;
        int speed = DEFAULT_SPEED;
        try
        {
            range = Integer.parseInt(inputRange.getText());
            speed = Integer.parseInt(inputSpeed.getText());
        }
        catch (final NumberFormatException exception)
        {
            Log.getLogger().warn("Unable to parse Multi-Piston range or speed; using default values", exception);
        }

        if (pos != null)
        {
            MultiPistonClientNetworking.sendConfiguration(pos, input, output, range, speed);
        }
        close();
    }
}
