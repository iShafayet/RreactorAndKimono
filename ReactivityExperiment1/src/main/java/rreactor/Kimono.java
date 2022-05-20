package rreactor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class Kimono<T> {

    private KimonoState state = KimonoState.IDLE;

    private Kimono<?> parent = null;

    private Kimono<?> next = null;
    private KimonoOperator operator = null;

    private Object inputValue = null;

    private T now_value = null;
    private Function<Object, T> map_fn;
    private Function<Object, Kimono<T>> chain_fn;
    private Consumer<T> sideEffect_fn;
    private CompletableFuture<T> produce_future;

    public static <U> Kimono<U> now(U value) {
        var kimono = new Kimono<U>();
        kimono.operator = KimonoOperator.NOW;
        kimono.now_value = value;
        return kimono;
    }

    public <U> Kimono<U> map(Function<T, U> fn) {
        var kimono = new Kimono<U>();
        this.next = kimono;
        kimono.parent = this;
        kimono.operator = KimonoOperator.MAP;
        kimono.map_fn = (Function<Object, U>) fn;
        return kimono;
    }

    public <U> Kimono<U> chain(Function<T, Kimono<U>> fn) {
        var kimono = new Kimono<U>();
        this.next = kimono;
        kimono.parent = this;
        kimono.operator = KimonoOperator.CHAIN;
        kimono.chain_fn = (Function<Object, Kimono<U>>) fn;
        return kimono;
    }

    public Kimono<T> sideEffect(Consumer<T> fn) {
        var kimono = new Kimono<T>();
        this.next = kimono;
        kimono.parent = this;
        kimono.operator = KimonoOperator.SIDE_EFFECT;
        kimono.sideEffect_fn = fn;
        return kimono;
    }

    public void run() {
        Rreactor.eventLoop.execute(() -> {
            writeLog("run()");
            if (this.parent != null) {
                writeLog("run() - proxying to parent");
                this.parent.run();
                return;
            }
            writeLog("run() - sending to be executed");
            Rreactor.registerKimonoForExecution(this);
        });
    }

    protected void executeInternally() {
        writeLog("executeInternally() - start");
        this.state = KimonoState.STARTED;

        writeLog("executeInternally() - predicate: %s", this.operator.name());
        if (this.operator == KimonoOperator.NOW) {
            var nextInputValue = this.now_value;
            writeLog("nextInputValue: %s", nextInputValue);
            if (this.next != null) {
                this.next.inputValue = nextInputValue;
                Rreactor.registerKimonoForExecution(this.next);
            }
        } else if (this.operator == KimonoOperator.MAP) {
            var nextInputValue = map_fn.apply(this.inputValue);
            writeLog("nextInputValue: %s", nextInputValue);
            if (this.next != null) {
                this.next.inputValue = nextInputValue;
                Rreactor.registerKimonoForExecution(this.next);
            }
        } else if (this.operator == KimonoOperator.SIDE_EFFECT) {
            sideEffect_fn.accept((T) this.inputValue);
            var nextInputValue = this.inputValue;
            writeLog("nextInputValue: %s", nextInputValue);
            if (this.next != null) {
                this.next.inputValue = nextInputValue;
                Rreactor.registerKimonoForExecution(this.next);
            }
        } else if (this.operator == KimonoOperator.CHAIN) {
            var nextMono = chain_fn.apply(this.inputValue);
            nextMono.next = this.next;
            nextMono.inputValue = this.inputValue;
            nextMono.run();
        } else if (this.operator == KimonoOperator.PRODUCE_FUTURE) {
            produce_future.thenAcceptAsync(nextInputValue -> {
                writeLog("nextInputValue: %s", nextInputValue);
                if (this.next != null) {
                    this.next.inputValue = nextInputValue;
                    Rreactor.registerKimonoForExecution(this.next);
                }
            }, Rreactor.eventLoop);
        }
        this.state = KimonoState.ENDED;
        writeLog("executeInternally() - end");
        Rreactor.executionCompletedForKimono(this);
    }

    public static <U> Kimono<U> produce(CompletableFuture<U> future) {
        var kimono = new Kimono<U>();
        kimono.operator = KimonoOperator.PRODUCE_FUTURE;
        kimono.produce_future = future;
        return kimono;
    }

    private void writeLog(String pattern, Object... value) {
        if (Rreactor.logLevel == LogLevel.LOG) {
            var prefix = String.format("t:%s c:%s<%s> \t", Thread.currentThread().getName(), "KIMONO", this.hashCode());
            System.out.println(String.format(prefix + pattern, value));
        }
    }

}