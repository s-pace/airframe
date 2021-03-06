package wvlet.airframe.http.okhttp

import okhttp3.{Interceptor, Response}
import wvlet.airframe.control.ResultClass
import wvlet.airframe.control.Retry.RetryContext
import wvlet.airframe.http.HttpClientMaxRetryException

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class OkHttpRetryInterceptor(retry: RetryContext) extends Interceptor {

  private def dispatch(retryContext: RetryContext, chain: Interceptor.Chain): Response = {
    val request = chain.request()

    val response = Try(chain.proceed(request))
    val resultClass = response match {
      case Success(r) =>
        try {
          retryContext.resultClassifier(r)
        } catch {
          case NonFatal(e) =>
            retryContext.errorClassifier(e)
        }
      case Failure(e) =>
        retryContext.errorClassifier(e)
    }

    resultClass match {
      case ResultClass.Succeeded =>
        response.get
      case ResultClass.Failed(isRetryable, cause, extraWait) =>
        if (!retryContext.canContinue) {
          // Reached the max retry
          throw HttpClientMaxRetryException(OkHttpResponseWrapper(response.toOption.orNull), retryContext, cause)
        } else if (!isRetryable) {
          // Non-retryable failure
          throw cause
        } else {
          // Update the retry count
          val nextRetryContext = retryContext.withExtraWait(extraWait).nextRetry(cause)
          // Wait until the next retry
          Thread.sleep(nextRetryContext.nextWaitMillis)
          // Run the same request again
          dispatch(nextRetryContext, chain)
        }
    }
  }

  override def intercept(chain: Interceptor.Chain): Response = {
    val request      = chain.request()
    val retryContext = retry.init(Option(request))
    dispatch(retryContext, chain)
  }
}
