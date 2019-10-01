module derive4j.fj {
    requires static auto.service.annotations;
    requires com.squareup.javapoet;
    requires derive4j.processor.api;

    provides org.derive4j.processor.api.DerivatorFactory
        with org.derive4j.processor.fj.FunctionalJavaTypeClassesDerivations;
}
