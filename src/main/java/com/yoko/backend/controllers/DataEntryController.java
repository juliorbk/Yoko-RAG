package com.yoko.backend.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data-entry")
public class DataEntryController {

  private final VectorStore vectorStore;

  public DataEntryController(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  /**
   * Endpoint to load data from the vector store.
   *
   * @return a ResponseEntity with a string indicating if the data was loaded successfully.
   */
  @PostMapping("/load-data")
  public ResponseEntity<String> loadData() {
    List<Document> documentosUneg = List.of(
      // ============================================================
      // DOCUMENTOS UNEG - Universidad Nacional Experimental de Guayana
      // Formato: new Document(contenido, Map.of(metadatos))
      // ============================================================

      // ============================================================
      // SECCIÓN 1: INFORMACIÓN GENERAL DE LA UNEG
      // ============================================================

      new Document(
        "La Universidad Nacional Experimental de Guayana (UNEG) es una universidad pública ubicada en el estado Bolívar, Venezuela, con sede principal en Ciudad Guayana. Fue fundada el 9 de marzo de 1982 mediante decreto presidencial N° 1.432 del Presidente Luis Herrera Campins.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "historia"
        )
      ),
      new Document(
        "La UNEG fue concebida como Centro de Educación Superior de carácter regional basado en tres principios fundamentales: Experimentalidad, Democratización y Regionalización. Su propósito original fue consolidar el proceso de desarrollo regional de la zona de Guayana, proporcionando profesionales formados en la región.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "principios_institucionales"
        )
      ),
      new Document(
        "La UNEG celebra su aniversario de fundación el 9 de marzo. Esta fecha es conocida como el Día de la UNEG y da nombre a la Orden al Mérito '9 de Marzo', distinción honorífica que otorga la institución a quienes han destacado por sus aportes a la universidad y a la región.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "cultura_institucional"
        )
      ),
      new Document(
        "La misión de la UNEG es formar ciudadanos, intelectuales y líderes para la transformación socio-cultural y técnico-científica que aseguren el desarrollo social y económico sustentable, con respeto y protección al ambiente.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "mision"
        )
      ),
      new Document(
        "La UNEG cuenta con sedes en las siguientes ciudades del estado Bolívar: Ciudad Guayana (sede principal, Av. Las Américas, Puerto Ordaz), Ciudad Bolívar (Jardín Botánico), Upata, El Callao, Santa Elena de Uairén, Guasipati, Caicara del Orinoco y El Palmar.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "sedes"
        )
      ),
      new Document(
        "La sede principal de la UNEG está ubicada en el Edificio General de Seguros, Avenida Las Américas, Puerto Ordaz, Estado Bolívar, Venezuela. Teléfono: +58 (0286) 7137131. RIF: G-20003343-6.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "contacto_sede_principal"
        )
      ),
      new Document(
        "Las actividades de pregrado de la UNEG comenzaron en 1987 con el Curso Introductorio. En 1988 se iniciaron las primeras carreras: Administración y Contaduría, Ingeniería en Informática e Ingeniería Industrial. En 1989 se incorporaron Ingeniería en Industrias Forestales y Educación Integral.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "historia_academica"
        )
      ),
      new Document(
        "La UNEG ofrece 14 proyectos de carrera a nivel de pregrado distribuidos en sus distintas sedes. En la sede de Ciudad Guayana (Puerto Ordaz) se dictan: Licenciatura en Contaduría Pública, Licenciatura en Administración de Empresas, Licenciatura en Administración de Banca y Finanzas, Licenciatura en Ciencias Fiscales, Educación Integral, Ingeniería Industrial, Ingeniería en Informática e Ingeniería en Industrias Forestales.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "carreras_ciudad_guayana"
        )
      ),
      new Document(
        "En la sede de Ciudad Bolívar, la UNEG ofrece: Licenciatura en Contaduría Pública, Licenciatura en Administración de Empresas, Licenciatura en Administración de Banca y Finanzas, Licenciatura en Ciencias Fiscales, Educación Integral, Educación mención Educación Física Deporte y Recreación, Educación mención Lengua y Literatura, Educación mención Matemática, TSU en Turismo y TSU en Alojamiento Turístico.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "carreras_ciudad_bolivar"
        )
      ),
      new Document(
        "En la sede de Upata, la UNEG ofrece la carrera de Tecnología en Producción Agropecuaria, iniciada en 1996. En Santa Elena de Uairén se dictó la Licenciatura en Letras mención Lengua Española y Portuguesa, en convenio con la Universidad Federal Roraima de Brasil. En Guasipati y El Callao se ofrecen carreras a nivel de Técnico Superior en Administración y Contaduría, y Licenciatura en Educación Integral.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "carreras_sedes_interior"
        )
      ),
      new Document(
        "La UNEG ofrece programas de postgrado que incluyen Maestrías en: Gerencia (menciones Finanzas, Operaciones y Producción, Mercadeo y Ventas), Ciencias de los Materiales, Gerencia de Recursos Humanos, Ciencias Ambientales, Gestión Educativa, Enseñanza de la Matemática y Medicina del Trabajo mención Salud Ocupacional.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "programas_postgrado"
        )
      ),
      new Document(
        "La UNEG dispone de sistemas en línea para sus comunidades estudiantil y académica. Entre los sistemas estudiantiles destacan: el Sistema de Inscripción de Pregrado (SIP), el Sistema de Apoyo a los Servicios Estudiantiles (SASE), el Sistema de Pasantía y el Sistema de Servicio Comunitario. Entre los académicos: PRISMA (Plataforma de Registro y Manejo de Contenido Académico), Sistema Integral de Seguimiento Académico (SISA) y Sistema de Trabajo de Grado Web.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "sistemas_en_linea"
        )
      ),
      new Document(
        "La UNEG promueve activamente las actividades culturales, deportivas y de extensión como parte de la formación integral del estudiante. Cuenta con el Centro de Excursionismo Ecológico (CEEUNEG), asociaciones estudiantiles por carrera como la Asociación de Estudiantes de Ingeniería Industrial (ASEIIND), y una Coordinación de Cultura que supervisa las manifestaciones artísticas e identitarias de la comunidad universitaria.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "cultura_y_extension"
        )
      ),
      new Document(
        "La UNEG posee un Fondo Editorial que gestiona la publicación de libros y revistas académicas. También cuenta con CIDAR (Centro de Información, Documentación y Archivo Regional), su sistema de biblioteca institucional, accesible en línea a través de cidar.uneg.edu.ve.",
        Map.of(
          "categoria",
          "informacion_general",
          "fuente",
          "reglamento_general_uneg",
          "subcategoria",
          "biblioteca_y_publicaciones"
        )
      ),
      // ============================================================
      // SECCIÓN 2: REGLAMENTOS ACADÉMICOS
      // ============================================================

      // --- REGLAMENTO DEL PERSONAL ACADÉMICO ---

      new Document(
        "El Personal Académico de la UNEG está constituido por quienes cumplen funciones docentes, de investigación y de extensión. Se clasifica en: Miembros Ordinarios (Instructor, Asistente, Agregado, Asociado y Titular), Miembros Especiales (contratados, docentes libres, auxiliares académicos), Miembros Honorarios y Jubilados.",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_personal_academico",
          "subcategoria",
          "clasificacion"
        )
      ),
      new Document(
        "Los tipos de dedicación del personal académico de la UNEG son: Dedicación Exclusiva (38 horas semanales, incompatible con cualquier otra actividad remunerada), Tiempo Completo (34 horas, incompatible con actividades que afecten la eficiencia universitaria), Medio Tiempo (17 horas) y Tiempo Convencional (6 horas, exclusivo para profesionales activos del sector productivo).",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_personal_academico",
          "subcategoria",
          "dedicacion"
        )
      ),
      new Document(
        "El ingreso al Personal Académico Ordinario de la UNEG se realiza mediante Concurso de Oposición. Para aprobarlo, el aspirante debe haber cursado al menos 20 unidades de crédito de postgrado con promedio igual o superior a 8 puntos, presentar y defender un trabajo de mérito, y cumplir con su Plan Individual de Formación Académica.",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_personal_academico",
          "subcategoria",
          "ingreso"
        )
      ),
      new Document(
        "Para ascender en el escalafón académico de la UNEG se requiere: de Instructor a Asistente, mínimo 2 años y trabajo de mérito; de Asistente a Agregado, mínimo 4 años y título de Doctor, Magíster o Especialista; de Agregado a Asociado, mínimo 4 años y título de Doctor; de Asociado a Titular, mínimo 5 años y trabajo de ascenso.",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_personal_academico",
          "subcategoria",
          "ascenso_escalafon"
        )
      ),
      new Document(
        "Todo miembro del personal académico de la UNEG debe mantener anualmente un índice académico igual o superior a 7,5 puntos. Un índice inferior activa un período de observación de un año. Si al término de ese año el índice sigue siendo deficiente, se puede proceder a la remoción del cargo mediante decisión del Consejo Universitario.",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_personal_academico",
          "subcategoria",
          "desempeno_academico"
        )
      ),
      new Document(
        "Los miembros ordinarios del Personal Académico con categoría de Agregado o superior, y al menos 6 años ininterrumpidos a Tiempo Completo o Dedicación Exclusiva, tienen derecho a un año sabático con goce de sueldo para dedicarse a producción académica. Este beneficio no es acumulable y no puede exceder el 10% del personal del departamento simultáneamente.",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_personal_academico",
          "subcategoria",
          "año_sabatico"
        )
      ),
      new Document(
        "Las becas del Personal Académico de la UNEG son otorgadas para estudios doctorales. Requisitos: ser Profesor Agregado con al menos 4 años de servicio ordinario a Tiempo Completo o Dedicación Exclusiva, estar al día con el ascenso correspondiente, pertenecer a un centro de investigación UNEG, y no estar en los últimos 5 años para jubilarse. La duración máxima es de 4 años.",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_personal_academico",
          "subcategoria",
          "becas_personal_academico"
        )
      ),
      new Document(
        "El becario del Personal Académico de la UNEG está obligado a servir a la institución, tras concluir su beca, por un tiempo no inferior a su duración y con la misma dedicación con que la disfrutó. El incumplimiento obliga a reintegrar todas las erogaciones realizadas por la UNEG, incluyendo pasajes y matrículas.",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_personal_academico",
          "subcategoria",
          "obligaciones_becario"
        )
      ),
      new Document(
        "Son obligaciones del personal académico de la UNEG: asistir puntualmente a clases, permanecer en el lugar de trabajo el tiempo de su dedicación, elaborar programas de asignaturas y someterlos a aprobación, participar en jurados académicos cuando sea designado, mejorar constantemente su capacidad científica y pedagógica, y cumplir los calendarios académicos aprobados.",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_personal_academico",
          "subcategoria",
          "obligaciones"
        )
      ),
      new Document(
        "Son derechos del personal académico de la UNEG: elegir y ser elegidos para cargos universitarios, solicitar y recibir capacitación académica, disfrutar del año sabático, cambiar de tipo de dedicación, acceder a bibliotecas y laboratorios, recibir obvenciones por producción intelectual y participar en todas las actividades del cogobierno universitario.",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_personal_academico",
          "subcategoria",
          "derechos"
        )
      ),
      // --- REGLAMENTO DE CENTROS DE INVESTIGACIÓN ---

      new Document(
        "La UNEG cuenta con Centros de Investigación regulados por su propio reglamento. Estos centros son unidades académicas orientadas a promover, organizar y ejecutar investigaciones pertinentes a las necesidades regionales y nacionales, articuladas con los proyectos académicos de pregrado y postgrado.",
        Map.of(
          "categoria",
          "investigacion",
          "fuente",
          "reglamento_centros_investigacion",
          "subcategoria",
          "centros_investigacion"
        )
      ),
      // --- REGLAMENTO DE PREPARADORES ---

      new Document(
        "La UNEG regula la figura de los Preparadores Docentes y de Investigación mediante reglamento especial. Los preparadores son estudiantes avanzados de la institución que apoyan al personal académico en actividades de docencia e investigación, bajo supervisión directa de un profesor ordinario.",
        Map.of(
          "categoria",
          "investigacion",
          "fuente",
          "reglamento_preparadores",
          "subcategoria",
          "preparadores"
        )
      ),
      // --- APOYO FINANCIERO POSTGRADO ---

      new Document(
        "La UNEG tiene un reglamento específico para otorgar apoyo financiero al personal académico que realiza estudios de postgrado en el país. Este beneficio busca fortalecer la formación del profesorado y está sujeto a la disponibilidad presupuestaria institucional y al cumplimiento de requisitos académicos.",
        Map.of(
          "categoria",
          "personal_academico",
          "fuente",
          "reglamento_apoyo_financiero_postgrado",
          "subcategoria",
          "apoyo_financiero"
        )
      ),
      // ============================================================
      // SECCIÓN 3: REGLAMENTOS ESTUDIANTILES
      // ============================================================

      // --- REGLAMENTO ESTUDIANTIL ---

      new Document(
        "Los estudiantes de la UNEG se clasifican en: aspirantes (admitidos para el Curso Introductorio), regulares (inscritos en carreras de pregrado), especiales (inscritos en educación permanente o acreditación de aprendizaje) y de postgrado (inscritos en Doctorado, Maestría o Especialización, regidos por el Reglamento de Postgrado).",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "clasificacion_estudiantes"
        )
      ),
      new Document(
        "Son deberes de los estudiantes de la UNEG: asumir la responsabilidad de su aprendizaje con enfoque andragógico, asistir puntualmente a clases y evaluaciones, mantener la disciplina universitaria, participar en actividades científicas, culturales y deportivas, cuidar las instalaciones y el patrimonio universitario, y participar en las elecciones universitarias según reglamento.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "deberes"
        )
      ),
      new Document(
        "Son derechos de los estudiantes de la UNEG respecto a la educación: recibir formación integral científica y humanística, orientación vocacional y adaptación académica, consultar a profesores y consejeros, cambiar de carrera una vez aprobados al menos dos semestres previa autorización del Consejo Universitario, y recibir reconocimientos, becas y distinciones por rendimiento académico.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "derechos_educacion"
        )
      ),
      new Document(
        "Son derechos de los estudiantes de la UNEG respecto a la evaluación: ser evaluados de forma justa e imparcial, conocer los errores cometidos en sus evaluaciones y recibir orientación para corregirlos, y presentar evaluaciones diferidas cuando estén representando a la universidad en eventos académicos, culturales, científicos o deportivos.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "derechos_evaluacion"
        )
      ),
      new Document(
        "Son derechos de los estudiantes de la UNEG respecto a los servicios: utilizar los servicios de protección de salud, económica y social previo cumplimiento de requisitos, y disponer de instalaciones, equipamiento y dotación suficientes para el cumplimiento de actividades académicas.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "derechos_servicios"
        )
      ),
      new Document(
        "Los estudiantes de la UNEG tienen derecho a: constituir asociaciones estudiantiles de diversa índole, obtener su reconocimiento institucional, y elegir y ser elegidos en los procesos electorales establecidos en los reglamentos universitarios.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "derechos_asociacion"
        )
      ),
      new Document(
        "El régimen disciplinario de la UNEG clasifica las faltas estudiantiles en tres niveles: graves (delitos comprobados, agresión física, reincidencia en 3 faltas menos graves en 1 año), menos graves (actos indecorosos o perturbadores del orden, reincidencia en 2 faltas leves en 1 año) y leves (incumplimiento de deberes académicos).",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "faltas_disciplinarias"
        )
      ),
      new Document(
        "Las sanciones disciplinarias estudiantiles en la UNEG son: para falta grave, expulsión definitiva o suspensión de 1 a 2 años con pérdida de lapsos; para falta menos grave, suspensión de 6 meses a 1 año con pérdida de lapsos; para falta leve, amonestación escrita. Las faltas graves no prescriben; las menos graves prescriben a los 6 meses; las leves a los 3 meses.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "sanciones"
        )
      ),
      new Document(
        "El procedimiento disciplinario de la UNEG garantiza el derecho al debido proceso. Las sanciones por faltas leves pueden ser impuestas por personal académico o directivo. Las faltas graves y menos graves requieren sustanciación de expediente por un instructor designado, opinión de la Consultoría Jurídica, y decisión del Consejo Académico con dos tercios de sus miembros.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "procedimiento_disciplinario"
        )
      ),
      new Document(
        "Todo estudiante sancionado en la UNEG tiene derecho a solicitar reconsideración en los 15 días hábiles siguientes a la notificación. Si la instancia no responde en 15 días, puede apelar. Las sanciones individuales apelan ante el Consejo Académico; las del Consejo Académico ante el Consejo Universitario; las de éste ante el Consejo de Apelaciones.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "recursos_apelacion"
        )
      ),
      new Document(
        "Las asociaciones estudiantiles de la UNEG tienen como objetivo coadyuvar al mejoramiento académico y promover actividades culturales, deportivas, recreativas y de autogestión. Solo pueden integrarse estudiantes regulares sin sanciones disciplinarias vigentes. Deben solicitar reconocimiento ante el Consejo Universitario presentando documento constitutivo y estatutos.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantil",
          "subcategoria",
          "asociaciones_estudiantiles"
        )
      ),
      // --- REGLAMENTO DE EVALUACIÓN DEL DESEMPEÑO ESTUDIANTIL ---

      new Document(
        "La UNEG cuenta con un Reglamento de Evaluación del Desempeño Estudiantil que establece los criterios, métodos e instrumentos para evaluar el rendimiento académico de los estudiantes de pregrado. Este reglamento rige las condiciones de aprobación, reprobación, diferimiento de evaluaciones y el sistema de calificaciones.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_evaluacion_estudiantil",
          "subcategoria",
          "evaluacion"
        )
      ),
      // --- REGLAMENTO DE TRABAJO DE GRADO ---

      new Document(
        "La UNEG regula mediante reglamento especial los Trabajos de Grado de Pregrado, que constituyen el requisito académico final para la obtención del título universitario. Este reglamento establece los tipos de trabajos aceptados, los requisitos para tutores y jurados, los lapsos para defensa, y los criterios de aprobación.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_trabajo_grado_pregrado",
          "subcategoria",
          "trabajo_grado"
        )
      ),
      // --- REGLAMENTO DE PASANTÍAS ---

      new Document(
        "La UNEG regula las pasantías estudiantiles mediante reglamento específico (última versión actualizada al 02-04-2018). Las pasantías constituyen una actividad académica obligatoria que permite al estudiante aplicar los conocimientos adquiridos en un entorno laboral real, bajo supervisión conjunta de la universidad y la empresa receptora.",
        Map.of(
          "categoria",
          "pasantias",
          "fuente",
          "reglamento_pasantias",
          "subcategoria",
          "definicion"
        )
      ),
      // --- REGLAMENTO DE ESTUDIOS DE PREGRADO ---

      new Document(
        "El Reglamento de Estudios de Pregrado de la UNEG establece las normas que rigen el proceso de formación académica de los estudiantes de pregrado, incluyendo las condiciones de inscripción, el régimen académico y curricular, los lapsos académicos, las equivalencias, el Curso Introductorio y las condiciones de permanencia en la institución.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudios_pregrado",
          "subcategoria",
          "regimen_pregrado"
        )
      ),
      // --- REGLAMENTO DEL CIVA ---

      new Document(
        "La UNEG ofrece el Curso Intensivo Vacacional (CIVA-UNEG), regulado por su propio reglamento. El CIVA permite a los estudiantes regulares cursar asignaturas durante el período vacacional, con el objetivo de adelantar materias o recuperar asignaturas aplazadas, sujeto a disponibilidad académica y cumplimiento de requisitos previos.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_civa",
          "subcategoria",
          "curso_intensivo_vacacional"
        )
      ),
      // --- REGLAMENTO DE ASIGNACIÓN SOCIOECONÓMICA ---

      new Document(
        "La UNEG cuenta con programas de asignación socioeconómica para estudiantes de pregrado, regulados por reglamento. Estos programas comprenden becas, ayudantías y apoyos económicos destinados a garantizar la permanencia de estudiantes en situación de vulnerabilidad socioeconómica, sujetos a cumplimiento de requisitos académicos mínimos.",
        Map.of(
          "categoria",
          "bienestar_estudiantil",
          "fuente",
          "reglamento_asignacion_socioeconomica",
          "subcategoria",
          "becas_estudiantiles"
        )
      ),
      // --- REGLAMENTO DEL COMEDOR ESTUDIANTIL ---

      new Document(
        "El Programa Comedor Estudiantil de la UNEG es un servicio de bienestar universitario regulado por reglamento. Está destinado a brindar alimentación subsidiada a los estudiantes de pregrado, con prioridad para quienes presenten condiciones socioeconómicas de mayor necesidad. El acceso requiere inscripción y cumplimiento de los requisitos establecidos.",
        Map.of(
          "categoria",
          "bienestar_estudiantil",
          "fuente",
          "reglamento_comedor_estudiantil",
          "subcategoria",
          "comedor"
        )
      ),
      // --- REGLAMENTO DE APORTES A ESTUDIANTES ---

      new Document(
        "La UNEG regula el otorgamiento de aportes económicos a estudiantes de pregrado, como complemento a los programas de asignación socioeconómica. Estos aportes están dirigidos a cubrir necesidades específicas como materiales académicos, transporte o alimentación, y están condicionados al rendimiento académico y situación económica del solicitante.",
        Map.of(
          "categoria",
          "bienestar_estudiantil",
          "fuente",
          "reglamento_aportes_estudiantes",
          "subcategoria",
          "aportes_economicos"
        )
      ),
      // --- REGLAMENTO DEL SERVICIO COMUNITARIO ---

      new Document(
        "El Servicio Comunitario del Estudiante de la UNEG está regulado por reglamento especial y es obligatorio para todos los estudiantes de pregrado conforme a la Ley de Servicio Comunitario venezolana. Consiste en la aplicación de los conocimientos y habilidades adquiridos en beneficio de comunidades en situación de vulnerabilidad, y su cumplimiento es requisito para la graduación.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_servicio_comunitario",
          "subcategoria",
          "servicio_comunitario"
        )
      ),
      // --- REGLAMENTO DE ESTUDIOS DE POSTGRADO ---

      new Document(
        "El Reglamento de Estudios de Postgrado de la UNEG rige los programas de Maestría, Especialización y Doctorado. Establece los requisitos de admisión, inscripción, permanencia, evaluación, elaboración de trabajo de grado y obtención de títulos. Los estudiantes de postgrado se rigen por este reglamento especial, distinto al reglamento estudiantil de pregrado.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudios_postgrado",
          "subcategoria",
          "postgrado"
        )
      ),
      // --- REGLAMENTO DEL PROGRAMA BECA TRABAJO ---

      new Document(
        "La UNEG dispone del Programa Beca Trabajo, regulado por reglamento. Este programa permite a estudiantes de pregrado con buen rendimiento académico y necesidad económica comprobada, realizar labores de apoyo administrativo o académico dentro de la institución a cambio de una asignación económica mensual.",
        Map.of(
          "categoria",
          "bienestar_estudiantil",
          "fuente",
          "reglamento_beca_trabajo",
          "subcategoria",
          "beca_trabajo"
        )
      ),
      // --- REGLAMENTO DE ESTUDIANTES ASESORES ---

      new Document(
        "La UNEG regula la figura del Estudiante Asesor mediante reglamento especial. Los estudiantes asesores son alumnos con alto rendimiento académico que brindan orientación y apoyo a sus compañeros en materias o áreas específicas. Esta figura complementa la labor del preparador docente y promueve el aprendizaje colaborativo.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_estudiantes_asesores",
          "subcategoria",
          "estudiantes_asesores"
        )
      ),
      // --- REGLAMENTO DE ACREDITACIÓN DE APRENDIZAJE POR EXPERIENCIA ---

      new Document(
        "La UNEG dispone de un reglamento para la Acreditación de Aprendizaje por Experiencia, que permite a personas con conocimientos adquiridos fuera del sistema formal de educación, obtener reconocimiento académico por dichos saberes, previo proceso de evaluación. Este mecanismo es parte del carácter experimental e inclusivo de la institución.",
        Map.of(
          "categoria",
          "reglamento_estudiantil",
          "fuente",
          "reglamento_acreditacion_aprendizaje",
          "subcategoria",
          "acreditacion_experiencia"
        )
      )
    );
    vectorStore.add(documentosUneg);
    return ResponseEntity.ok("Data loaded successfully");
  }
}
