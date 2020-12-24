#lang racket

(define (make-counter)
  (define count 0)
  (lambda ()
    (set! count (+ count 1))
    count))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; For creating a CSV file of HIP list                    ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (prepare-hip-list source-file)
  (call-with-input-file source-file
    (lambda (in)
      (let iter ([line (read-line in)]
		 [result '()])
	(if (eof-object? line)
	    (reverse result)
	    (let* ([values (map string->number
				(string-split (string-trim line) ","))]
		   [hip (first values)]
		   [ra-h (second values)]
		   [ra-m (third values)]
		   [ra-s (fourth values)]
		   [dec-sign (fifth values)]
		   [dec-d (sixth values)]
		   [dec-m (seventh values)]
		   [dec-s (eighth values)]
		   [mag (ninth values)])
	      (iter (read-line in)
		    (if (< mag 6.0)
			(cons `(,hip
				,(+ ra-h (/ ra-m 60.0) (/ ra-s 3600.0))
				,((if (zero? dec-sign) - +)
				  (+ dec-d (/ dec-m 60.0) (/ dec-s 3600.0)))
				,mag)
			      result)
			result))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; For creating a CSV file of constellation lines         ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (prepare-constellation-lines source-file)
  (call-with-input-file source-file
    (lambda (in)
      (let iter ([line (read-line in)]
		 [result '()])
	(if (eof-object? line)
	    (reverse result)
	    (let* ([values (map string->number
				(string-split (string-trim line) ","))]
		   [hip1 (second values)]
		   [hip2 (third values)])
	      (iter (read-line in)
		    (cons `(,hip1 ,hip2) result))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; For creating a CSV file of the patterns of the Milky Way;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define array-north (vector-map
		     (lambda (x) (make-vector 300 0))
		     (make-vector 300)))

(define array-south (vector-map
		     (lambda (x) (make-vector 300 0))
		     (make-vector 300)))

(define (distance-from-north-pole x)
  (- 90 x))

(define (distance-from-south-pole x)
  (+ 90 x))

(define (prepare-array source-file array distance direction)
  (call-with-input-file source-file
    (lambda (in)
      (let iter ([line (read-line in)])
	(if (eof-object? line)
	    ": Array of the Milky Way is ready."
	    (let* ([values (map string->number
				(string-split (string-trim line) ","))]
		   [ra (direction (first values))]
		   [dec (second values)]
		   [polar (make-polar (distance dec) (* ra (/ pi 12)))]
		   [x (inexact->exact (round (+ (- (imag-part polar)) 150)))]
		   [y (inexact->exact (round (+ (- (real-part polar)) 150)))])
	      (if (and (>= x 0)
		       (< x 300)
		       (>= y 0)
		       (< y 300))
		  (let ([current-value (vector-ref (vector-ref array y) x)])
		    (vector-set! (vector-ref array y)
				 x
				 (+ current-value (third values))))
		  '())
	      (iter (read-line in))))))))

(define (create-milkyway-pattern-file array name min-value)
  (let ([id-counter (make-counter)]
	[filename (format "modified-csv/milkyway-pattern-~a~a.csv"
			  name
			  min-value)])
    (call-with-output-file filename
      (lambda (out)
	(for ([y (in-range 300)])
	  (for ([x (in-range 300)])
	    (let ([v (vector-ref (vector-ref array y) x)])
	      (if (> v min-value)
		  (fprintf out
			   "~a,~a,~a,~a~n"
			   (id-counter)
			   (- x 150)
			   (- y 150)
			   v)
		  '()))))
	(format "'~a' is ready." filename))
      #:exists 'replace)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Prepares HIP list                                      ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; hip-list should be defined at top level, because it is needed for
;; preparing constellation lines.
(define hip-list (prepare-hip-list "source-csv/hip_lite_major.csv"))

(let ([target-file "modified-csv/hip_lite_major_converted.csv"])
  (call-with-output-file target-file
    (lambda (out)
      (for-each (lambda (line)
		  (fprintf out
			   "~a,~a,~a,~a~n"
			   (first line)
			   (second line)
			   (third line)
			   (fourth line)))
		hip-list))
    #:exists 'replace)

  (printf "'~a' is ready.~n" target-file))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Prepares constellation lines                           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (find-position-from-hip hip)
  (let ([result (assv hip hip-list)])
    (if result
	(cdr result)
	result)))

(let ([id-counter (make-counter)]
      [source-file "source-csv/hip_constellation_line.csv"]
      [target-file "modified-csv/constellation_lines.csv"])
  (call-with-output-file target-file
    (lambda (out)
      (for-each (lambda (line)
		  (let ([start (find-position-from-hip (first line))]
			[end (find-position-from-hip (second line))])
		    (and start
			 end
			 (fprintf out "~a,~a,~a,~a,~a~n"
				  (id-counter)
				  (first start)
				  (second start)
				  (first end)
				  (second end)))))
		(prepare-constellation-lines source-file)))
    #:exists 'replace)

  (printf "'~a' is ready.~n" target-file))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Prepares the patterns of the Milky Way                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(let ([source-file "source-csv/milkyway.txt"]
      [min-mag 200]
      [parameters `((,array-north ,distance-from-north-pole ,- "north")
		    (,array-south ,distance-from-south-pole ,+ "south"))])
  (for-each (lambda (x)
	      (define dot-id (make-counter))
	      (let ([array (first x)]
		    [distance (second x)]
		    [direction (third x)]
		    [label (fourth x)])
		;; Prepares array
		(displayln (prepare-array source-file array distance direction))
		
		;; Shows results
		(printf ": Number of entries (~a): ~a~n"
			label
			(apply +
			       (map
				(lambda (x)
				  (vector-length
				   (vector-filter
				    (lambda (x) (> x min-mag)) x)))
				(vector->list array))))
		;; Writes CSV files
		(displayln
		 (create-milkyway-pattern-file array label min-mag))))
	    parameters))
