## Servers ##

Here's what we have in aws:
 * tools server (tools.delegation.org.nz) (m4.xlarge, 140GiB)
  * archiva
  * squirrel mail
  * jump host
  * jenkins
  * webserver (nginx)
 * selenity (selenity.delegation.org.nz) (m4.xlarge, 60GiB)
  * selenity
  * selenity-ui
  * selenity jenkins
 * selenium (selenium.delegation.org.nz) (t2.medium, 30GiB)
  * selenium runners
 * gitlab (git.tools.delegation.org.nz) (t2.medium, 100GiB + backup volume 20GiB)
  * git
  * gitlab
 * Elastic IPs for tools, gitlab

Each environment (pocdev and poc)
 * web (m3.medium, 8GiB)
  * webserver (apache) - proxies traffic for www and sso
 * app1 (c3.large, 8GiB)
  * OpenAM
 * app2 (c3.large, 8GiB)
  * the Delegation api/app
 * dat1 (c3.large, 8GiB)
  * postgres
 * test (m3.medium, 8GiB)
  * test harnesses
  * demo apps
  * apache for test
 * Elastic IPs for web (and test?)
