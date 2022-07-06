package team.creative.littletiles.common.math.face;

public enum LittleFaceState {
    
    UNLOADED {
        
        @Override
        public boolean coveredFully() {
            return false;
        }
        
        @Override
        public boolean partially() {
            return false;
        }
        
        @Override
        public boolean outside() {
            return false;
        }
        
    },
    INSIDE_UNCOVERED {
        
        @Override
        public boolean outside() {
            return false;
        }
        
        @Override
        public boolean coveredFully() {
            return false;
        }
        
        @Override
        public boolean partially() {
            return false;
        }
        
    },
    INSIDE_PARTIALLY_COVERED {
        
        @Override
        public boolean outside() {
            return false;
        }
        
        @Override
        public boolean coveredFully() {
            return false;
        }
        
        @Override
        public boolean partially() {
            return true;
        }
        
    },
    INSIDE_COVERED {
        
        @Override
        public boolean outside() {
            return false;
        }
        
        @Override
        public boolean coveredFully() {
            return true;
        }
        
        @Override
        public boolean partially() {
            return false;
        }
        
    },
    OUTSIDE_UNCOVERED {
        
        @Override
        public boolean outside() {
            return true;
        }
        
        @Override
        public boolean coveredFully() {
            return false;
        }
        
        @Override
        public boolean partially() {
            return false;
        }
        
    },
    OUTSIDE_PARTIALLY_COVERED {
        
        @Override
        public boolean outside() {
            return true;
        }
        
        @Override
        public boolean coveredFully() {
            return false;
        }
        
        @Override
        public boolean partially() {
            return true;
        }
        
    },
    OUTISDE_COVERED {
        
        @Override
        public boolean outside() {
            return true;
        }
        
        @Override
        public boolean coveredFully() {
            return true;
        }
        
        @Override
        public boolean partially() {
            return false;
        }
        
    };
    
    public abstract boolean coveredFully();
    
    public abstract boolean partially();
    
    public abstract boolean outside();
    
}
