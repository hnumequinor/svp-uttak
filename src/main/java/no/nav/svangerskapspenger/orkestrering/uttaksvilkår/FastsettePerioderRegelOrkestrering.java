package no.nav.svangerskapspenger.orkestrering.uttaksvilkår;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.svangerskapspenger.regler.uttak.Regelresultat;
import no.nav.svangerskapspenger.regler.uttak.fastsetteperiode.FastsettePeriodeRegel;
import no.nav.svangerskapspenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.svangerskapspenger.domene.søknad.AvklarteDatoer;
import no.nav.svangerskapspenger.domene.resultat.Uttaksperiode;
import no.nav.svangerskapspenger.domene.resultat.Uttaksperioder;
import no.nav.svangerskapspenger.orkestrering.uttaksvilkår.feil.UttakRegelFeil;
import no.nav.svangerskapspenger.orkestrering.uttaksvilkår.jackson.JacksonJsonConfig;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class FastsettePerioderRegelOrkestrering {

    private final JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    public void fastsettePerioder(AvklarteDatoer avklarteDatoer, Uttaksperioder uttaksperioder) {
        //Først knekk opp perioder på alle potensielle knekkpunkter
        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(avklarteDatoer);
        uttaksperioder.knekk(knekkpunkter);

        //Fastsett perioder
        FastsettePeriodeRegel regel = new FastsettePeriodeRegel();
        uttaksperioder.alleArbeidsforhold().forEach(arbeidsforhold -> uttaksperioder.perioder(arbeidsforhold).forEach(periode -> fastsettPeriode(regel, avklarteDatoer, periode)));
    }



    private void fastsettPeriode(FastsettePeriodeRegel regel, AvklarteDatoer avklarteDatoer, Uttaksperiode periode) {
        var grunnlag = new FastsettePeriodeGrunnlag(avklarteDatoer, periode);
        var evaluering = regel.evaluer(grunnlag);
        var inputJson = toJson(grunnlag);
        var regelJson = EvaluationSerializer.asJson(evaluering);
        var regelresultat = new Regelresultat(evaluering);
        var utfallType = regelresultat.getUtfallType();
        var årsak = regelresultat.getPeriodeÅrsak();

        switch (utfallType) {
            case AVSLÅTT:
                periode.avslå(årsak, inputJson, regelJson);
                break;
            case INNVILGET:
                periode.innvilg(årsak, inputJson, regelJson);
                break;
            default:
                throw new UnsupportedOperationException(String.format("Ukjent utfalltype: %s", utfallType.name()));
        }
    }

    private String toJson(FastsettePeriodeGrunnlag grunnlag) {
        try {
            return jacksonJsonConfig.toJson(grunnlag);
        } catch (JsonProcessingException e) {
            throw new UttakRegelFeil("Kunne ikke serialisere regelinput for avklaring av uttaksperioder.", e);
        }
    }

}
