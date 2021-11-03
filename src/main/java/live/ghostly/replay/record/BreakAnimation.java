package live.ghostly.replay.record;

import com.comphenix.protocol.wrappers.BlockPosition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class BreakAnimation {

    @Getter final BlockPosition position;
    @Setter @Getter int i = 0;

}