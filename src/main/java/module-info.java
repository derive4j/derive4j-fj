/*
 * Copyright (c) 2017, Jean-Baptiste Giraudeau <jb@giraudeau.info>
 *
 * This file is part of "Derive4J - Functional Java".
 *
 * "Derive4J - Functional Java" is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * "Derive4J - Functional Java" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Derive4J - Functional Java".  If not, see <http://www.gnu.org/licenses/>.
 */
module derive4j.fj {
    requires static auto.service.annotations;
    requires derive4j.processor.api;
    requires java.compiler;
    requires com.squareup.javapoet;

    provides org.derive4j.processor.api.DerivatorFactory
        with org.derive4j.processor.fj.FunctionalJavaTypeClassesDerivations;
}
