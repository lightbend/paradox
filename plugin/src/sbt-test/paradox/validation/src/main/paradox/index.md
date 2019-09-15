[docs site path link](test.html#frag)
[docs site path link name frag](test.html#namefrag)

[outside docs path link](../outside.html#frag)

This URL should 404 (or otherwise fail)
https://github.com/lightbend/thisrepodoesnotexist
This URL should 301 (or otherwise fail)
https://lightbend.com

Including this in build means we depend on github.com being up. Probably ok.
https://github.com/