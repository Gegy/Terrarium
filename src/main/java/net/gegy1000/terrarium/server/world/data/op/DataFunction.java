package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.justnow.future.Future;
import net.gegy1000.terrarium.server.world.data.DataContext;
import net.gegy1000.terrarium.server.world.data.DataView;

import java.util.Optional;

public interface DataFunction<T> {
    Future<Optional<T>> apply(DataView view, DataContext ctx);
}
