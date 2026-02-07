package team.creative.littletiles.common.placement.selection;

public record SelectionParameters(boolean includeVanilla, boolean includeCB, boolean includeLT, boolean includeBE, boolean rememberStructure) {
    
    public SelectionParameters(boolean includeVanilla, boolean includeCB, boolean includeLT, boolean rememberStructure) {
        this(includeVanilla, includeCB, includeLT, includeCB || includeLT, rememberStructure);
    }
}
