package org.cercanias.crawl;
import java.util.*;
import java.util.stream.Collectors;

public class TrainStation {
    private static Map<String, String> stations = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("aguilas", "35602"),
            new AbstractMap.SimpleEntry<>("alcala", "70103"),
            new AbstractMap.SimpleEntry<>("alcalaUni", "70107"),
            new AbstractMap.SimpleEntry<>("alcorcon", "35605"),
            new AbstractMap.SimpleEntry<>("alpedrete", "12002"),
            new AbstractMap.SimpleEntry<>("aluche", "35600"),
            new AbstractMap.SimpleEntry<>("aranjuez", "60200"),
            new AbstractMap.SimpleEntry<>("aravaca", "10001"),
            new AbstractMap.SimpleEntry<>("asamblea", "70002"),
            new AbstractMap.SimpleEntry<>("atocha", "18000"),
            new AbstractMap.SimpleEntry<>("azuqueca", "70105"),
            new AbstractMap.SimpleEntry<>("aarrial", "10010"),
            new AbstractMap.SimpleEntry<>("cantoblanco", "17009"),
            new AbstractMap.SimpleEntry<>("cercedilla", "12006"),
            new AbstractMap.SimpleEntry<>("chamartín", "17000"),
            new AbstractMap.SimpleEntry<>("ciempozuelos", "60105"),
            new AbstractMap.SimpleEntry<>("collado", "12004"),
            new AbstractMap.SimpleEntry<>("colmenar", "17005"),
            new AbstractMap.SimpleEntry<>("coslada", "70108"),
            new AbstractMap.SimpleEntry<>("cotos", "12023"),
            new AbstractMap.SimpleEntry<>("cuatrovientos", "35603"),
            new AbstractMap.SimpleEntry<>("delicias", "18004"),
            new AbstractMap.SimpleEntry<>("docedeoctubre", "35702"),
            new AbstractMap.SimpleEntry<>("embajadores", "35609"),
            new AbstractMap.SimpleEntry<>("escorial", "10203"),
            new AbstractMap.SimpleEntry<>("fuencarral", "17001"),
            new AbstractMap.SimpleEntry<>("fuenlabrada", "35002"),
            new AbstractMap.SimpleEntry<>("fuentedelaMora", "98003"),
            new AbstractMap.SimpleEntry<>("galapagar", "10104"),
            new AbstractMap.SimpleEntry<>("garena", "70111"),
            new AbstractMap.SimpleEntry<>("getafe3", "37011"),
            new AbstractMap.SimpleEntry<>("getafecentro", "37002"),
            new AbstractMap.SimpleEntry<>("getafeindustrial", "60102"),
            new AbstractMap.SimpleEntry<>("goloso", "17003"),
            new AbstractMap.SimpleEntry<>("guadalajara", "70200"),
            new AbstractMap.SimpleEntry<>("humanes", "35012"),
            new AbstractMap.SimpleEntry<>("justafreire", "35601"),
            new AbstractMap.SimpleEntry<>("laguna", "35608"),
            new AbstractMap.SimpleEntry<>("margaritasuni", "37010"),
            new AbstractMap.SimpleEntry<>("matas", "10101"),
            new AbstractMap.SimpleEntry<>("meco", "70104"),
            new AbstractMap.SimpleEntry<>("mendezalvaro", "18003"),
            new AbstractMap.SimpleEntry<>("mendezalvaropv", "35701"),
            new AbstractMap.SimpleEntry<>("mirasierra", "97200"),
            new AbstractMap.SimpleEntry<>("molinos", "12005"),
            new AbstractMap.SimpleEntry<>("mostoles", "35606"),
            new AbstractMap.SimpleEntry<>("mostolessoto", "35607"),
            new AbstractMap.SimpleEntry<>("negrales", "12001"),
            new AbstractMap.SimpleEntry<>("nuevosministerios", "18002"),
            new AbstractMap.SimpleEntry<>("orcasitas", "35703"),
            new AbstractMap.SimpleEntry<>("parla", "37012"),
            new AbstractMap.SimpleEntry<>("parquepolvoranca", "35011"),
            new AbstractMap.SimpleEntry<>("pinardelasrozas", "10100"),
            new AbstractMap.SimpleEntry<>("pinto", "60103"),
            new AbstractMap.SimpleEntry<>("piramides", "18005"),
            new AbstractMap.SimpleEntry<>("pitis", "97100"),
            new AbstractMap.SimpleEntry<>("pozo", "70003"),
            new AbstractMap.SimpleEntry<>("pozuelo", "10002"),
            new AbstractMap.SimpleEntry<>("principepio", "10000"),
            new AbstractMap.SimpleEntry<>("puentealcocer", "35704"),
            new AbstractMap.SimpleEntry<>("ramonycajal", "97201"),
            new AbstractMap.SimpleEntry<>("recoletos", "18001"),
            new AbstractMap.SimpleEntry<>("robledochavela", "10205"),
            new AbstractMap.SimpleEntry<>("sancristobal", "60107"),
            new AbstractMap.SimpleEntry<>("sancristobalindustrial", "60101"),
            new AbstractMap.SimpleEntry<>("sanfernando", "70101"),
            new AbstractMap.SimpleEntry<>("sanjosedevalderas", "35604"),
            new AbstractMap.SimpleEntry<>("sanyago", "10201"),
            new AbstractMap.SimpleEntry<>("santaeugenia", "70109"),
            new AbstractMap.SimpleEntry<>("santamariadelaalameda", "10206"),
            new AbstractMap.SimpleEntry<>("sol", "18101"),
            new AbstractMap.SimpleEntry<>("sotodelhenares", "70112"),
            new AbstractMap.SimpleEntry<>("t4", "98305"),
            new AbstractMap.SimpleEntry<>("torrejonardoz", "70102"),
            new AbstractMap.SimpleEntry<>("torrelodones", "10103"),
            new AbstractMap.SimpleEntry<>("trescantos", "17004"),
            new AbstractMap.SimpleEntry<>("unipcomillas", "19001"),
            new AbstractMap.SimpleEntry<>("valdebebas", "98304"),
            new AbstractMap.SimpleEntry<>("valdelasfuentes", "19002"),
            new AbstractMap.SimpleEntry<>("valdemoro", "60104"),
            new AbstractMap.SimpleEntry<>("vallecas", "70001"),
            new AbstractMap.SimpleEntry<>("vicalvaro", "70100"),
            new AbstractMap.SimpleEntry<>("villalba", "10200"),
            new AbstractMap.SimpleEntry<>("villaverdealto", "37001"),
            new AbstractMap.SimpleEntry<>("villaverdebajo", "60100"),
            new AbstractMap.SimpleEntry<>("zarzalejo", "10204"),
            new AbstractMap.SimpleEntry<>("zarzaquemada", "35009")
    );

    public static List<String> getAllStations(){
        return stations.keySet().stream().sorted().collect(Collectors.toList());
    }

    public static boolean stationExists(String station){
        return stations.containsKey(station.toLowerCase());
    }

    public static Optional<String> getValue(String station){
        if(stations.containsKey(station.toLowerCase())){
            return Optional.of(stations.get(station.toLowerCase()));
        }
        return Optional.empty();
    }

}
