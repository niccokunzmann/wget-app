# Notes on Development


## New Release

For a new release
- in [app/build.gradle]
    - increase the `versionCode` (Example: `1`)
    - increase the `versionName` (Example: `1.0`)
- add a file with the `versionCode` to [metadata/en/changelogs](metadata/en/changelogs) (Example `1.txt`). [Documentation][in-app-metadata]
- commit it and push it to `master`
    ```
    git commit -am"version 1.0"
    ```
- tag the commit with the version and a `v` at the front (Example `v1.0`).
    ```
    git tag v1.0
    git push origin v1.0
    ```

Now, F-Droid will pull the code and create a new release.

[in-app-metadata]: https://f-droid.org/en/docs/All_About_Descriptions_Graphics_and_Screenshots/#in-the-apps-source-repository
[app/build.gradle]: https://github.com/niccokunzmann/wget-app/blob/master/app/build.gradle#L10
