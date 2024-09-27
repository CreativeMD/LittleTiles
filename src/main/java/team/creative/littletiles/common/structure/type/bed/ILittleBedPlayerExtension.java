package team.creative.littletiles.common.structure.type.bed;

public interface ILittleBedPlayerExtension {
    
    public LittleBed getBed();
    
    public void setBed(LittleBed bed);
    
    public void setSleepingCounter(int counter);
    
    public boolean setPositionToBed();
    
}
