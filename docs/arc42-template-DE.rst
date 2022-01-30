**Über arc42**

arc42, das Template zur Dokumentation von Software- und
Systemarchitekturen.

Erstellt von Dr. Gernot Starke, Dr. Peter Hruschka und Mitwirkenden.

Template Revision: 7.0 DE (asciidoc-based), January 2017

© We acknowledge that this document uses material from the arc42
architecture template, http://www.arc42.de. Created by Dr. Peter
Hruschka & Dr. Gernot Starke.

.. _section-introduction-and-goals:

Einführung und Ziele
====================

.. __aufgabenstellung:

Aufgabenstellung
----------------

.. __qualit_tsziele:

Qualitätsziele
--------------

.. __stakeholder:

Stakeholder
-----------

+-----------------+-----------------+-----------------------------------+
| Rolle           | Kontakt         | Erwartungshaltung                 |
+=================+=================+===================================+
| *<Rolle-1>*     | *<Kontakt-1>*   | *<Erwartung-1>*                   |
+-----------------+-----------------+-----------------------------------+
| *<Rolle-2>*     | *<Kontakt-2>*   | *<Erwartung-2>*                   |
+-----------------+-----------------+-----------------------------------+

.. _section-architecture-constraints:

Randbedingungen
===============

.. _section-system-scope-and-context:

Kontextabgrenzung
=================

.. __fachlicher_kontext:

Fachlicher Kontext
------------------

**<Diagramm und/oder Tabelle>**

**<optional: Erläuterung der externen fachlichen Schnittstellen>**

.. __technischer_kontext:

Technischer Kontext
-------------------

**<Diagramm oder Tabelle>**

**<optional: Erläuterung der externen technischen Schnittstellen>**

**<Mapping fachliche auf technische Schnittstellen>**

.. _section-solution-strategy:

Lösungsstrategie
================

.. _section-building-block-view:

Bausteinsicht
=============

.. __whitebox_gesamtsystem:

Whitebox Gesamtsystem
---------------------

**<Übersichtsdiagramm>**

Begründung
   *<Erläuternder Text>*

Enthaltene Bausteine
   *<Beschreibung der enthaltenen Bausteine (Blackboxen)>*

Wichtige Schnittstellen
   *<Beschreibung wichtiger Schnittstellen>*

.. ___name_blackbox_1:

<Name Blackbox 1>
~~~~~~~~~~~~~~~~~

*<Zweck/Verantwortung>*

*<Schnittstelle(n)>*

*<(Optional) Qualitäts-/Leistungsmerkmale>*

*<(Optional) Ablageort/Datei(en)>*

*<(Optional) Erfüllte Anforderungen>*

*<(optional) Offene Punkte/Probleme/Risiken>*

.. ___name_blackbox_2:

<Name Blackbox 2>
~~~~~~~~~~~~~~~~~

*<Blackbox-Template>*

.. ___name_blackbox_n:

<Name Blackbox n>
~~~~~~~~~~~~~~~~~

*<Blackbox-Template>*

.. ___name_schnittstelle_1:

<Name Schnittstelle 1>
~~~~~~~~~~~~~~~~~~~~~~

…

.. ___name_schnittstelle_m:

<Name Schnittstelle m>
~~~~~~~~~~~~~~~~~~~~~~

.. __ebene_2:

Ebene 2
-------

.. __whitebox_emphasis_baustein_1_emphasis:

Whitebox *<Baustein 1>*
~~~~~~~~~~~~~~~~~~~~~~~

*<Whitebox-Template>*

.. __whitebox_emphasis_baustein_2_emphasis:

Whitebox *<Baustein 2>*
~~~~~~~~~~~~~~~~~~~~~~~

*<Whitebox-Template>*

…

.. __whitebox_emphasis_baustein_m_emphasis:

Whitebox *<Baustein m>*
~~~~~~~~~~~~~~~~~~~~~~~

*<Whitebox-Template>*

.. __ebene_3:

Ebene 3
-------

.. __whitebox_baustein_x_1:

Whitebox <_Baustein x.1_>
~~~~~~~~~~~~~~~~~~~~~~~~~

*<Whitebox-Template>*

.. __whitebox_baustein_x_2:

Whitebox <_Baustein x.2_>
~~~~~~~~~~~~~~~~~~~~~~~~~

*<Whitebox-Template>*

.. __whitebox_baustein_y_1:

Whitebox <_Baustein y.1_>
~~~~~~~~~~~~~~~~~~~~~~~~~

*<Whitebox-Template>*

.. _section-runtime-view:

Laufzeitsicht
=============

.. ___emphasis_bezeichnung_laufzeitszenario_1_emphasis:

*<Bezeichnung Laufzeitszenario 1>*
----------------------------------

-  <hier Laufzeitdiagramm oder Ablaufbeschreibung einfügen>

-  <hier Besonderheiten bei dem Zusammenspiel der Bausteine in diesem
   Szenario erläutern>

.. ___emphasis_bezeichnung_laufzeitszenario_2_emphasis:

*<Bezeichnung Laufzeitszenario 2>*
----------------------------------

…

.. ___emphasis_bezeichnung_laufzeitszenario_n_emphasis:

*<Bezeichnung Laufzeitszenario n>*
----------------------------------

…

.. _section-deployment-view:

Verteilungssicht
================

.. __infrastruktur_ebene_1:

Infrastruktur Ebene 1
---------------------

**<Übersichtsdiagramm>**

Begründung
   *<Erläuternder Text>*

Qualitäts- und/oder Leistungsmerkmale
   *<Erläuternder Text>*

Zuordnung von Bausteinen zu Infrastruktur
   *<Beschreibung der Zuordnung>*

.. __infrastruktur_ebene_2:

Infrastruktur Ebene 2
---------------------

.. ___emphasis_infrastrukturelement_1_emphasis:

*<Infrastrukturelement 1>*
~~~~~~~~~~~~~~~~~~~~~~~~~~

*<Diagramm + Erläuterungen>*

.. ___emphasis_infrastrukturelement_2_emphasis:

*<Infrastrukturelement 2>*
~~~~~~~~~~~~~~~~~~~~~~~~~~

*<Diagramm + Erläuterungen>*

…

.. ___emphasis_infrastrukturelement_n_emphasis:

*<Infrastrukturelement n>*
~~~~~~~~~~~~~~~~~~~~~~~~~~

*<Diagramm + Erläuterungen>*

.. _section-concepts:

Querschnittliche Konzepte
=========================

.. ___emphasis_konzept_1_emphasis:

*<Konzept 1>*
-------------

*<Erklärung>*

.. ___emphasis_konzept_2_emphasis:

*<Konzept 2>*
-------------

*<Erklärung>*

…

.. ___emphasis_konzept_n_emphasis:

*<Konzept n>*
-------------

*<Erklärung>*

.. _section-design-decisions:

Entwurfsentscheidungen
======================

.. _section-quality-scenarios:

Qualitätsanforderungen
======================

.. __qualit_tsbaum:

Qualitätsbaum
-------------

.. __qualit_tsszenarien:

Qualitätsszenarien
------------------

.. _section-technical-risks:

Risiken und technische Schulden
===============================

.. _section-glossary:

Glossar
=======

+-----------------------+-----------------------------------------------+
| Begriff               | Definition                                    |
+=======================+===============================================+
| *<Begriff-1>*         | *<Definition-1>*                              |
+-----------------------+-----------------------------------------------+
| *<Begriff-2*          | *<Definition-2>*                              |
+-----------------------+-----------------------------------------------+

.. |arc42| image:: images/arc42-logo.png
