package com.medievalfactions.domain;

import java.util.*;
import static com.medievalfactions.domain.MFAdvancedSystems.*;
import static com.medievalfactions.domain.MFGameCatalog.*;

/** Explicit-consent marriages and resource-backed medical treatment. */
public final class MFFamilyCare {
    private MFFamilyCare() {}
    public enum MarriageStatus { PROPOSED, MARRIED, DECLINED, DISSOLVED }
    public enum Condition { INJURY, INFECTION, EXHAUSTION, CHILDBIRTH, CHRONIC }
    public enum MedicalStatus { OPEN, TREATED, RECOVERING, RESOLVED, DECEASED }

    public record Marriage(UUID id, UUID personA, UUID personB, UUID proposer, boolean consentA,
                           boolean consentB, MarriageStatus status, long createdDay, long marriedDay) {
        public Marriage {
            Objects.requireNonNull(id); Objects.requireNonNull(personA); Objects.requireNonNull(personB);
            Objects.requireNonNull(proposer); Objects.requireNonNull(status);
            if(personA.equals(personB) || (!proposer.equals(personA)&&!proposer.equals(personB)) || createdDay<0 || marriedDay< -1)
                throw new IllegalArgumentException("Invalid marriage");
        }
        public Marriage answer(UUID respondent, boolean consent, long day) {
            if(status!=MarriageStatus.PROPOSED || respondent==null || day<createdDay
                    || (!respondent.equals(personA)&&!respondent.equals(personB)) || respondent.equals(proposer))
                throw new IllegalStateException("Invalid marriage response");
            if(!consent) return new Marriage(id,personA,personB,proposer,consentA,consentB,MarriageStatus.DECLINED,createdDay,-1);
            boolean a=consentA||respondent.equals(personA); boolean b=consentB||respondent.equals(personB);
            return new Marriage(id,personA,personB,proposer,a,b,a&&b?MarriageStatus.MARRIED:MarriageStatus.PROPOSED,
                    createdDay,a&&b?day:-1);
        }
        public Marriage dissolve() {
            if(status!=MarriageStatus.MARRIED) throw new IllegalStateException("Only a marriage can be dissolved");
            return new Marriage(id,personA,personB,proposer,consentA,consentB,MarriageStatus.DISSOLVED,createdDay,marriedDay);
        }
    }

    public static Optional<Marriage> propose(Person a, Person b, Relationship relationship, UUID proposer, long day) {
        if(a==null || b==null || relationship==null || proposer==null || day<0 || !a.adult() || !b.adult()
                || !relationship.mutuallyConsenting() || relationship.status()!=RelationshipStatus.COURTING || relationship.trust()<60
                || !((relationship.personA().equals(a.id())&&relationship.personB().equals(b.id()))
                    || (relationship.personB().equals(a.id())&&relationship.personA().equals(b.id())))
                || (!proposer.equals(a.id())&&!proposer.equals(b.id())) || a.race()!=b.race()) return Optional.empty();
        return Optional.of(new Marriage(UUID.randomUUID(),a.id(),b.id(),proposer,proposer.equals(a.id()),proposer.equals(b.id()),
                MarriageStatus.PROPOSED,day,-1));
    }

    public record MedicalCase(UUID id, UUID patientId, Condition condition, int severity, int infection,
                              MedicalStatus status, long openedDay, long lastTreatmentDay) {
        public MedicalCase {
            Objects.requireNonNull(id); Objects.requireNonNull(patientId); Objects.requireNonNull(condition); Objects.requireNonNull(status);
            if(severity<0 || severity>100 || infection<0 || infection>100 || openedDay<0 || lastTreatmentDay< -1)
                throw new IllegalArgumentException("Invalid medical case");
        }
        public MedicalCase advanceDay(boolean safe, boolean fed) {
            if(status==MedicalStatus.RESOLVED || status==MedicalStatus.DECEASED) return this;
            int nextSeverity = safe && fed ? Math.max(0, severity - 3)
                    : Math.min(100, severity + (safe ? 1 : 3) + (fed ? 0 : 1));
            int nextInfection = safe && fed ? Math.max(0, infection - 2)
                    : Math.min(100, infection + (safe ? 1 : 4) + (fed ? 0 : 2));
            boolean fatal = !safe && !fed && nextSeverity >= 98 && nextInfection >= 98;
            MedicalStatus next = fatal ? MedicalStatus.DECEASED
                    : nextSeverity == 0 && nextInfection == 0 ? MedicalStatus.RESOLVED : MedicalStatus.RECOVERING;
            return new MedicalCase(id, patientId, condition, nextSeverity, nextInfection, next, openedDay, lastTreatmentDay);
        }
    }
    public record Clinic(int tier, int doctorSkill, int medicineStock, int beds) {
        public Clinic { if(tier<0 || tier>5 || doctorSkill<0 || doctorSkill>100 || medicineStock<0 || beds<0) throw new IllegalArgumentException("Invalid clinic"); }
    }
    public record TreatmentResult(MedicalCase medicalCase, Clinic clinic, int healthRestored, String message) {}

    /** Treatment consumes one real medicine unit; no treatment can succeed without a bed and doctor. */
    public static Optional<TreatmentResult> treat(MedicalCase medicalCase, Clinic clinic, long day) {
        if(medicalCase==null || clinic==null || day<0 || clinic.tier()==0 || clinic.doctorSkill()==0 || clinic.medicineStock()==0
                || clinic.beds()==0 || medicalCase.status()==MedicalStatus.RESOLVED || medicalCase.status()==MedicalStatus.DECEASED
                || medicalCase.lastTreatmentDay()==day) return Optional.empty();
        int efficacy=Math.max(5,Math.min(80,clinic.tier()*8+clinic.doctorSkill()/3));
        int severity=Math.max(0,medicalCase.severity()-efficacy);
        int infection=medicalCase.condition()==Condition.INFECTION?Math.max(0,medicalCase.infection()-efficacy/2):medicalCase.infection();
        MedicalStatus status=severity==0 && infection==0?MedicalStatus.RESOLVED:MedicalStatus.TREATED;
        MedicalCase next=new MedicalCase(medicalCase.id(),medicalCase.patientId(),medicalCase.condition(),severity,infection,status,
                medicalCase.openedDay(),day);
        Clinic stock=new Clinic(clinic.tier(),clinic.doctorSkill(),clinic.medicineStock()-1,clinic.beds());
        return Optional.of(new TreatmentResult(next,stock,Math.min(25,efficacy/3),"Tratamiento aplicado con consumo de medicina"));
    }
}
