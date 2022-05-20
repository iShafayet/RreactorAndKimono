package rreactor;

import java.util.function.Consumer;
import java.util.function.Function;

public class Kimono<T> {

    private KimonoState state = KimonoState.IDLE;

    private Kimono<?> parent = null;

    private Kimono<?> next = null;
    private KimonoOperator predicate = null;

    private Object inputValue = null;

    private T now_value = null;
    private Function<Object, T> map_fn;
    private Function<Object, Kimono<T>> chain_fn;
    private Consumer<T> sideEffect_fn;

    public static <U> Kimono<U> now(U value) {
        var kimono = new Kimono<U>();
        kimono.predicate = KimonoOperator.NOW;
        kimono.now_value = value;
        return kimono;
    }

    public <U> Kimono<U> map(Function<T, U> fn) {
        var kimono = new Kimono<U>();
        this.next = kimono;
        kimono.parent = this;
        kimono.predicate = KimonoOperator.MAP;
        kimono.map_fn = (Function<Object, U>) fn;
        return kimono;
    }

    public <U> Kimono<U> chain(Function<T, Kimono<U>> fn) {
        var kimono = new Kimono<U>();
        this.next = kimono;
        kimono.parent = this;
        kimono.predicate = KimonoOperator.CHAIN;
        kimono.chain_fn = (Function<Object, Kimono<U>>) fn;
        return kimono;
    }

    public Kimono<T> sideEffect(Consumer<T> fn) {
        var kimono = new Kimono<T>();
        this.next = kimono;
        kimono.parent = this;
        kimono.predicate = KimonoOperator.SIDE_EFFECT;
        kimono.sideEffect_fn = fn;
        return kimono;
    }

    public void run() {
        writeLog("run()");
        if (this.parent != null) {
            writeLog("run() - proxying to parent");
            this.parent.run();
            return;
        }
        writeLog("run() - sending to be executed");
        Rreactor.registerKimonoForExecution(this);
    }

    protected void executeInternally() {
        writeLog("executeInternally() - start");
        this.state = KimonoState.STARTED;

        writeLog("executeInternally() - predicate: %s", this.predicate.name());
        if (this.predicate == KimonoOperator.NOW) {
            var nextInputValue = this.now_value;
            writeLog("nextInputValue: %s", nextInputValue);
            if (this.next != null) {
                this.next.inputValue = nextInputValue;
                Rreactor.registerKimonoForExecution(this.next);
            }
        } else if (this.predicate == KimonoOperator.MAP) {
            var nextInputValue = map_fn.apply(this.inputValue);
            writeLog("nextInputValue: %s", nextInputValue);
            if (this.next != null) {
                this.next.inputValue = nextInputValue;
                Rreactor.registerKimonoForExecution(this.next);
            }
        } else if (this.predicate == KimonoOperator.SIDE_EFFECT) {
            sideEffect_fn.accept((T) this.inputValue);
            var nextInputValue = this.inputValue;
            writeLog("nextInputValue: %s", nextInputValue);
            if (this.next != null) {
                this.next.inputValue = nextInputValue;
                Rreactor.registerKimonoForExecution(this.next);
            }
        } else if (this.predicate == KimonoOperator.CHAIN) {
            var nextMono = chain_fn.apply(this.inputValue);
            nextMono.next = this.next;
            nextMono.inputValue = this.inputValue;
            nextMono.run();
//            Rreactor.registerKimonoForExecution(nextMono);
        }
        this.state = KimonoState.ENDED;
        writeLog("executeInternally() - end");
        Rreactor.executionCompletedForKimono(this);
    }

    private void writeLog(String pattern, Object... value) {
        if (Rreactor.logLevel == LogLevel.LOG) {
            System.out.println(String.format("KIMONO " + pattern, value));
        }
    }

}