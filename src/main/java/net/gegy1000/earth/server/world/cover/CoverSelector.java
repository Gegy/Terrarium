package net.gegy1000.earth.server.world.cover;

import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface CoverSelector extends Iterable<Cover> {
    boolean contains(Cover cover);

    default Stream<Cover> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    default CoverSelector or(CoverSelector rhs) {
        return new Or(this, rhs);
    }

    default CoverSelector and(CoverSelector rhs) {
        return new And(this, rhs);
    }

    default CoverSelector not() {
        return new Not(this);
    }

    class Or implements CoverSelector {
        private final CoverSelector a;
        private final CoverSelector b;

        private Set<Cover> collected;

        Or(CoverSelector a, CoverSelector b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean contains(Cover cover) {
            return this.a.contains(cover) || this.b.contains(cover);
        }

        @Override
        public Iterator<Cover> iterator() {
            if (this.collected == null) {
                this.collected = Sets.union(Sets.newHashSet(this.a), Sets.newHashSet(this.b));
            }
            return this.collected.iterator();
        }
    }

    class And implements CoverSelector {
        private final CoverSelector a;
        private final CoverSelector b;

        private Set<Cover> collected;

        And(CoverSelector a, CoverSelector b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean contains(Cover cover) {
            return this.a.contains(cover) && this.b.contains(cover);
        }

        @Override
        public Iterator<Cover> iterator() {
            if (this.collected == null) {
                this.collected = Sets.intersection(Sets.newHashSet(this.a), Sets.newHashSet(this.b));
            }
            return this.collected.iterator();
        }
    }

    class Not implements CoverSelector {
        private final CoverSelector not;
        private Set<Cover> collected;

        Not(CoverSelector not) {
            this.not = not;
        }

        @Override
        public boolean contains(Cover cover) {
            return !this.not.contains(cover);
        }

        @Override
        public Iterator<Cover> iterator() {
            if (this.collected == null) {
                this.collected = Sets.difference(Sets.newHashSet(Cover.values()), Sets.newHashSet(this.not));
            }
            return this.collected.iterator();
        }
    }
}
