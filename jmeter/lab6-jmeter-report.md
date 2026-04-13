# Lab 6.4: JMeter load testing for race-demo (`unsafe` vs `safe`)

## Scope
- Endpoint under test: `POST /api/movies/{id}/views/race-demo`
- Modes compared: `unsafe` and `safe`
- Goal: under equal load, `safe` should return `lostUpdates = 0`; `unsafe` should usually show lost updates.

## Test plan artifact
- JMeter plan: `jmeter/lab6-race-demo.jmx`

The plan includes:
- user variables: `host`, `port`, `movieId`, `threads=50`, `incrementsPerThread=5`, `requestsPerMode=5`;
- Thread Group A for `mode=unsafe`;
- Thread Group B for `mode=safe`;
- assertion `HTTP 200` for both modes;
- strict assertion for safe mode: `lostUpdates == 0`;
- listeners: Summary Report and Aggregate Report.

## Preconditions
- API + DB are running.
- `movieId` exists in DB.
- Use `./scripts/jmeter.sh` from this repository. It bootstraps Apache JMeter `5.6.3` into `/tmp/tools`.
- Do not use the Ubuntu `jmeter` package (`/usr/bin/jmeter`): on this machine it is `2.13.19691222`, which crashes on Java 17 and cannot load this test plan correctly.

## Run commands
```bash
mkdir -p jmeter/results

./scripts/jmeter.sh -n \
  -t jmeter/lab6-race-demo.jmx \
  -Jhost=localhost \
  -Jport=8080 \
  -JmovieId=113 \
  -Jthreads=50 \
  -JincrementsPerThread=5 \
  -JrequestsPerMode=5 \
  -Jsample_variables=mode,expectedCount,actualCount,lostUpdates,durationMs \
  -Jjmeter.save.saveservice.output_format=csv \
  -Jjmeter.save.saveservice.print_field_names=true \
  -l jmeter/results/lab6-race-run1.jtl

./scripts/jmeter.sh -g jmeter/results/lab6-race-run1.jtl -o jmeter/results/dashboard-run1
```

Repeat the non-GUI run command for `run2..run5` by changing output file names.

## Environment
- Date: 2026-04-01
- Project: `movie-catalog`
- Dataset: existing movie record (example `movieId=113`)
- Load profile per request: `threads=50`, `incrementsPerThread=5`.

## Results
> Current repository update adds reproducible test artifacts and commands.
> Numerical run results must be filled after execution on a machine with running API+DB and installed JMeter.

| Run | Mode | Throughput (req/s) | Avg latency (ms) | P95 latency (ms) | Error % | expectedCount | actualCount | lostUpdates |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| 1 | unsafe | TBD | TBD | TBD | TBD | 250 | TBD | TBD |
| 1 | safe   | TBD | TBD | TBD | TBD | 250 | TBD | 0 |
| 2 | unsafe | TBD | TBD | TBD | TBD | 250 | TBD | TBD |
| 2 | safe   | TBD | TBD | TBD | TBD | 250 | TBD | 0 |
| 3 | unsafe | TBD | TBD | TBD | TBD | 250 | TBD | TBD |
| 3 | safe   | TBD | TBD | TBD | TBD | 250 | TBD | 0 |
| 4 | unsafe | TBD | TBD | TBD | TBD | 250 | TBD | TBD |
| 4 | safe   | TBD | TBD | TBD | TBD | 250 | TBD | 0 |
| 5 | unsafe | TBD | TBD | TBD | TBD | 250 | TBD | TBD |
| 5 | safe   | TBD | TBD | TBD | TBD | 250 | TBD | 0 |

## Conclusion template
- `safe`: `lostUpdates` should remain `0` in all runs.
- `unsafe`: lost updates should appear in at least part of runs.
- With identical query parameters, this demonstrates the race condition and the effect of the synchronization fix.
