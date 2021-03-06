package edu.knuca.resmat.exam.taskflow

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.LongNumber
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.CirceSupport
import edu.knuca.resmat.exam.UserExamService
import edu.knuca.resmat.user.AuthenticatedUser
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class TaskFlowExamRoute(examService: UserExamService) extends CirceSupport {

  import edu.knuca.resmat.http.JsonProtocol._

  def route(userExamId: Long, stepSequence: Int, attemptId: Long)
           (implicit user: AuthenticatedUser, ec: ExecutionContext): Route =
    pathPrefix("task-flows" / LongNumber) { taskFlowId =>
      pathPrefix("steps") {
        pathPrefix("current") {
          complete(Future(examService.getCurrentTaskFlowStepDto(taskFlowId)))
        } ~
        pathPrefix(LongNumber) { taskFlowStepId =>
          pathPrefix("verify") {
            pathEndOrSingleSlash {
              (post & entity(as[String])) { answer =>
                complete(Future(
                  examService.verifyTaskFlowStepAnswer(
                    userExamId, stepSequence, attemptId, taskFlowId, taskFlowStepId, answer)
                ))
              }
            }
          }
        }
      }
    }

}
