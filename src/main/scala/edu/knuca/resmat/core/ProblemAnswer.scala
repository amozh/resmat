package edu.knuca.resmat.core

import breeze.linalg.DenseMatrix
import edu.knuca.resmat.core.crosssection.GeometryShape
import edu.knuca.resmat.core.ringplate.{CoordinateResult, ExtremeConditionsResult, ExtremeStressResult, GaussResult, ShiftAndForceResult}
import edu.knuca.resmat.exam.{ChartData, ChartSet, DynamicTable, DynamicTableRow, SmartValueStaticDouble, SmartValueStaticString}
import io.circe.generic.JsonCodec

//Both needed for ProblemAnswer
import io.circe.generic.auto._
import edu.knuca.resmat.http.JsonProtocol._

@JsonCodec sealed trait ProblemAnswer {
  protected val mapping: Map[String, Any]

  def get(key: String): Any = {
    mapping.get(key) match {
      case v: Some[Any] => v.get
      case v => throw new IllegalArgumentException(s"Key {$key} does not exist in ${this.getClass.getSimpleName}")
    }
  }

  /**
    * Works with both Double and Option[Double] values
    */
  def getDouble(key: String): Double = {
    mapping.get(key) match {
      case Some(value) => {
        value match {
          case v: Option[Double] =>
            v match {
              case Some(doubleValue) => doubleValue
              case _ => throw new IllegalArgumentException(s"No Double value found. Requested from ${this.getClass.getSimpleName} by key {$key}")
            }
          case v: Double => v
          case _ =>
            throw new IllegalArgumentException(s"No Double value found. Requested from ${this.getClass.getSimpleName} by key {$key}")
        }
      }
      case v => throw new IllegalArgumentException(s"{$v} is not a Double value. Requested from ${this.getClass.getSimpleName} by key {$key}")
    }
  }

  /**
    * Works with both Double and Option[Double] values
    */
  def getDoubleOpt(key: String): Option[Double] = {
    mapping.get(key).flatMap {
      case v: Option[Double] => v
      case v: Double => Some(v)
      case _ => None
    }
  }

  def getString(key: String): String = {
    mapping.get(key) match {
      case v: Some[String] => v.get
      case v => throw new IllegalArgumentException(s"{$v} is not a String value. Requested from ${this.getClass.getSimpleName} by key {$key}")
    }
  }
}

case class RingPlateProblemAnswer(del_t: Double,
  d_e: Double,
  r1: Array[Double],
  isStrengthGuaranteed: Boolean,
  gauss: GaussResult,
  shiftAndForce: ShiftAndForceResult,
  extremeStress: ExtremeStressResult,
  extremeConditions: ExtremeConditionsResult,
  coordinateResult: CoordinateResult,
  g1: DenseMatrix[Double]) extends ProblemAnswer {

  import RingPlateProblemAnswer.{Mapping => M}

  override protected val mapping: Map[String, Any] = Map(
    M.w_a -> extremeConditions.a.w,
    M.fi_a -> extremeConditions.a.fi,
    M.mr_a -> extremeConditions.a.mr,
    M.qr_a -> extremeConditions.a.qr,
    M.w_b -> extremeConditions.b.w,
    M.fi_b -> extremeConditions.b.fi,
    M.mr_b -> extremeConditions.b.mr,
    M.qr_b -> extremeConditions.b.qr,

    M.x1 -> Some(gauss.b2(0)),
    M.x2 -> Some(gauss.b2(1)),
    M.x3 -> Some(gauss.b2(2)),
    M.x4 -> Some(gauss.b2(3)),

    M.charts -> ChartSet("Епюри", Seq(
      ChartData("W Прогин (1/1000 м)",
        r1,
        shiftAndForce.w_1,
        true
      ),
      ChartData("{phi} Кут повороту (1/1000 рад)",
        r1,
        shiftAndForce.fi_1
      ),
      ChartData("Mr Радіальний момент (кН)",
        r1,
        shiftAndForce.mr_1,
        true
      ),
      ChartData("M{theta} Коловий момент (кН)",
        r1,
        shiftAndForce.mt_1,
        true
      ),
      ChartData("Qr Поперечна сила (кН/м)",
        r1,
        shiftAndForce.qr_1
      )
    )),

    M.chartsAsTable -> DynamicTable("Епюри в табличному вигляді", List(), List(
      DynamicTableRow("W", shiftAndForce.w_1.map(d => SmartValueStaticDouble(d)).toList),
      DynamicTableRow("{phi}", shiftAndForce.fi_1.map(d => SmartValueStaticDouble(d)).toList),
      DynamicTableRow("Mr", shiftAndForce.mr_1.map(d => SmartValueStaticDouble(d)).toList),
      DynamicTableRow("M{theta}", shiftAndForce.mt_1.map(d => SmartValueStaticDouble(d)).toList),
      DynamicTableRow("Qr", shiftAndForce.qr_1.map(d => SmartValueStaticDouble(d)).toList)
    )),

    M.r -> Some(coordinateResult.r),
    M.sigma_r -> Some(coordinateResult.qr),
    M.sigma_theta -> Some(coordinateResult.qt),
    M.sigma_eq -> Some(coordinateResult.qeq),
    M.tau_max -> Some(coordinateResult.tmax),

    M.g1_00 -> g1(0, 0),
    M.g1_01 -> g1(0, 1),
    M.g1_02 -> g1(0, 2),
    M.g1_03 -> g1(0, 3),
    M.g1_04 -> g1(0, 4),
    M.g1_10 -> g1(1, 0),
    M.g1_11 -> g1(1, 1),
    M.g1_12 -> g1(1, 2),
    M.g1_13 -> g1(1, 3),
    M.g1_14 -> g1(1, 4),
    M.g1_20 -> g1(2, 0),
    M.g1_21 -> g1(2, 1),
    M.g1_22 -> g1(2, 2),
    M.g1_23 -> g1(2, 3),
    M.g1_24 -> g1(2, 4),
    M.g1_30 -> g1(3, 0),
    M.g1_31 -> g1(3, 1),
    M.g1_32 -> g1(3, 2),
    M.g1_33 -> g1(3, 3),
    M.g1_34 -> g1(3, 4),

    M.w1_2 -> Some(shiftAndForce.w_1(2)),
    M.fi1_2 -> Some(shiftAndForce.fi_1(2)),
    M.mr1_2 -> Some(shiftAndForce.mr_1(2)),
    M.qr1_2 -> Some(shiftAndForce.qr_1(2)),

    M.d_e -> Some(d_e),

    M.isStrengthGuranteed -> (if(isStrengthGuaranteed) 1 else 0).toString
  )
}
object RingPlateProblemAnswer {
  object Mapping {
    val plateType = "plateType"

    val w_a = "w_a"
    val fi_a = "fi_a"
    val mr_a = "mr_a"
    val qr_a = "qr_a"
    val w_b = "w_b"
    val fi_b = "fi_b"
    val mr_b = "mr_b"
    val qr_b = "qr_b"

    val x1 = "x1"
    val x2 = "x2"
    val x3 = "x3"
    val x4 = "x4"

    val charts = "charts"
    val chartsAsTable = "chartsAsTable"

    val r = "r"
    val sigma_r = "sigma_r"
    val sigma_theta = "sigma_theta"
    val sigma_eq = "sigma_eq"
    val tau_max = "tau_max"

    val g1_00 = "g1_00"
    val g1_01 = "g1_01"
    val g1_02 = "g1_02"
    val g1_03 = "g1_03"
    val g1_04 = "g1_04"
    val g1_10 = "g1_10"
    val g1_11 = "g1_11"
    val g1_12 = "g1_12"
    val g1_13 = "g1_13"
    val g1_14 = "g1_14"
    val g1_20 = "g1_20"
    val g1_21 = "g1_21"
    val g1_22 = "g1_22"
    val g1_23 = "g1_23"
    val g1_24 = "g1_24"
    val g1_30 = "g1_30"
    val g1_31 = "g1_31"
    val g1_32 = "g1_32"
    val g1_33 = "g1_33"
    val g1_34 = "g1_34"

    val w1_2 = "w1_2"
    val fi1_2 = "fi1_2"
    val mr1_2 = "mr1_2"
    val qr1_2 = "qr1_2"

    val d_e = "d_e"

    val isStrengthGuranteed = "isStrengthGuranteed"
  }
}

case class CrossSectionProblemAnswer(
  shapes: Vector[GeometryShape],
  shapeGeometryCharacteristics: Vector[ShapeCalculatedData],
  centerOfGravity: CenterOfGravity,
  distanceBetweenCentralAxes: DistanceBetweenCentralAxes,
  centralMomentsOfInertia: CentralMomentsOfInertia,
  mainCoordinateSystem: MainCoordinateSystem,
  mainMomentsOfInertia: MainMomentsOfInertia,
  mainMomentsOfInertiaCheck: MainMomentsOfInertiaCheck,
  radiusesOfInertia: RadiusesOfInertia,
  finalDrawingShapes: FinalDrawingShapes
) extends ProblemAnswer {

  import edu.knuca.resmat.core.CrossSectionProblemAnswer.{Mapping => M}

  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._
  import edu.knuca.resmat.http.JsonProtocol._

  def round(v: Double) = BigDecimal(v).setScale(2, BigDecimal.RoundingMode.HALF_UP); 
  
  override protected val mapping: Map[String, Any] = {
    val shapeInputs = shapes.map(_.getShapeCalculatedData)
    val static = Map(
      M.amountOfShapes -> Some(shapeInputs.size.toDouble),
      M.shapeIdsDividedByComma -> shapeInputs.map(_.id).mkString(","),

      M.shapeCalculatedDataDynamicTable -> DynamicTable("Характеристики фігур", shapeInputs.map(_.shapeName).toList, List(
        DynamicTableRow("$A$", shapeInputs.map(i => SmartValueStaticDouble(i.square)).toList),
        DynamicTableRow("$I_y$", shapeInputs.map(i => SmartValueStaticDouble(i.I_y)).toList),
        DynamicTableRow("$I_z$", shapeInputs.map(i => SmartValueStaticDouble(i.I_z)).toList),
        DynamicTableRow("$I_{yz}$", shapeInputs.map(i => SmartValueStaticDouble(i.I_yz)).toList)
      )),

      M.s_z0 -> centerOfGravity.s_z0,
      M.s_y0 -> centerOfGravity.s_y0,
      M.sumOfSquares -> centerOfGravity.sumOfSquares,
      
      M.y_center -> centerOfGravity.y_center,
      M.z_center -> centerOfGravity.z_center,
      
      M.S_y_c -> distanceBetweenCentralAxes.S_y_c,
      M.S_z_c -> distanceBetweenCentralAxes.S_z_c,

      M.I_yc -> centralMomentsOfInertia.I_yc,
      M.I_zc -> centralMomentsOfInertia.I_zc,
      M.I_yzc -> centralMomentsOfInertia.I_yzc,

      M.alfaDegrees -> mainCoordinateSystem.alphaDegrees,

      M.I_u -> mainMomentsOfInertia.I_u,
      M.I_v -> mainMomentsOfInertia.I_v,

      M.I_max -> mainMomentsOfInertiaCheck.I_max,
      M.I_min -> mainMomentsOfInertiaCheck.I_min,

      M.i_u -> radiusesOfInertia.i_u,
      M.i_v -> radiusesOfInertia.i_v,
      
      M.finalDrawingShapes -> finalDrawingShapes.shapes.asJson.toString
    )
    val shapeDataMap: Map[String, String] = shapes.flatMap(s => {
      Map(
        M.Input.name(s.id) -> s.name,
        M.Input.title(s.id) -> s"Фігура ${s.id} - ${s.name}",
        M.Input.json(s.id) -> List(s).asJson.toString
      )
    }).toMap
    val inputsMap: Map[String, Option[Double]] = shapeInputs.flatMap(si => {
      Map(
        M.Input.id(si.id) -> Some(si.id.toDouble),
        M.Input.square(si.id) -> Some(si.square),
        M.Input.iy(si.id) -> Some(si.I_y),
        M.Input.iz(si.id) -> Some(si.I_z),
        M.Input.iyz(si.id) -> Some(si.I_yz),
        M.Input.yCenter(si.id) -> Some(si.y_center),
        M.Input.zCenter(si.id) -> Some(si.z_center)
      )
    }).toMap
    val aMap: Map[String, Double] = distanceBetweenCentralAxes.a.map(ai => M.a(ai.shapeId) -> ai.distance).toMap
    val bMap: Map[String, Double] = distanceBetweenCentralAxes.b.map(bi => M.b(bi.shapeId) -> bi.distance).toMap

    static ++ shapeDataMap ++ inputsMap ++ aMap ++ bMap
  }

  override def toString: String = {
    s"""
       |CrossSectionProblemAnswer
       |---------------------------------------------
       |${shapes.map(_.toString)}
       |${shapeGeometryCharacteristics.mkString("")}
       |$centerOfGravity
       |$centralMomentsOfInertia
       |$distanceBetweenCentralAxes
       |$mainCoordinateSystem
       |$mainMomentsOfInertia
       |$mainMomentsOfInertiaCheck
       |$radiusesOfInertia
       |$finalDrawingShapes
       |---------------------------------------------
       |""".stripMargin
  }
}
object CrossSectionProblemAnswer {

  object Mapping {

    object Input {
      val idKey = "id"
      def id(shapeId: Int) = s"${idKey}_$shapeId"
      val nameKey = "name"
      def name(shapeId: Int) = s"${nameKey}_$shapeId"
      val titleKey = "title"
      def title(shapeId: Int) = s"${titleKey}_$shapeId"
      val jsonKey = "json"
      def json(shapeId: Int) = s"${jsonKey}_$shapeId"
      val squareKey = "square"
      def square(shapeId: Int) = s"${squareKey}_$shapeId"
      val iyKey = "iy"
      def iy(shapeId: Int) = s"${iyKey}_$shapeId"
      val izKey = "iz"
      def iz(shapeId: Int) = s"${izKey}_$shapeId"
      val iyzKey = "iyz"
      def iyz(shapeId: Int) = s"${iyzKey}_$shapeId"
      val yCenterKey = "y_center"
      def yCenter(shapeId: Int) = s"${yCenterKey}_$shapeId"
      val zCenterKey = "z_center"
      def zCenter(shapeId: Int) = s"${zCenterKey}_$shapeId"
    }

    val amountOfShapes = "amountOfShapes" //TODO: not needed
    val shapeIdsDividedByComma = "shapeIdsDividedByComma"
    
    val shapeCalculatedDataDynamicTable = "shapeCalculatedDataDynamicTable"
    
    //CenterOfGravity
    val s_z0 = "s_z0"
    val s_y0 = "s_y0"
    val sumOfSquares = "sumOfSquares"
    val y_center = "y_center"
    val z_center = "z_center"
    
    //DistanceBetweenCentralAxes
    val S_y_c = "S_y_c"
    val S_z_c = "S_z_c"
    val aKey = "a"
    val bKey = "b"
    def a(shapeId: Int) = s"${aKey}_$shapeId"
    def b(shapeId: Int) = s"${bKey}_$shapeId"
    
    //CentralMomentsOfInertia - Загальні моменти інерції для всієї системи (відносно не повернутої системи)
    val I_yc = "I_yc"
    val I_zc = "I_zc"
    val I_yzc = "I_yzc"
    //MainCoordinateSystem - Положення головної системи координат U-V - центр в центрі ваги загальному
    val alfaDegrees = "alfaDegrees"
    //MainMomentsOfInertia - Головні моменти інерції (відносно повернутої системи)
    val I_u = "I_u"
    val I_v = "I_v"
    // I_uv == 0 в цій системі координат
    //MainMomentsOfInertiaCheck
    val I_max = "I_max"
    val I_min = "I_min"
    //RadiusesOfInertia
    val i_u = "i_u"
    val i_v = "i_v"

    val finalDrawingShapes = "finalDrawingShapes"
  }

}
object ProblemAnswer // to make JsonCodec work